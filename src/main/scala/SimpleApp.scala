// import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

sealed trait Foo
case class Bar(xs: Vector[String]) extends Foo
case class Qux(i: Int, d: Option[Double]) extends Foo

import cats.effect.{IO, IOApp, Resource, Sync, ExitCode}
import cats.effect.std.Console
import cats.syntax.all._
import java.io._

// function inputOutputStreams not needed
def inputStream(f: File): Resource[IO, FileInputStream] =
  // Resource.make {
  //   IO.blocking(new FileInputStream(f))                         // build
  // } { inStream =>
  //   IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit) // release
  // }
  Resource.fromAutoCloseable(IO(new FileInputStream(f)))

def inputStreamPoly[F[_]: Sync](f: File): Resource[F, FileInputStream] =
  Resource.make {
    Sync[F].blocking(new FileInputStream(f))
  } { inStream =>
    Sync[F].blocking(inStream.close()).handleErrorWith(_ => Sync[F].unit)
  }

def outputStream(f: File): Resource[IO, FileOutputStream] =
  Resource.make {
    IO.blocking(new FileOutputStream(f))                         // build
  } { outStream =>
    IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit) // release
  }

def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
  for {
    inStream  <- inputStream(in)
    outStream <- outputStream(out)
  } yield (inStream, outStream)

// transfer will do the real work


def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
  for {
    amount <- IO.blocking(origin.read(buffer, 0, buffer.size))
    count  <- if(amount > -1) IO.blocking(destination.write(buffer, 0, amount)) >> transmit(origin, destination, buffer, acc + amount)
              else IO.pure(acc) // End of read stream reached (by java.io.InputStream contract), nothing to write
  } yield count // Returns the actual amount of bytes transmitted // Returns the actual amount of bytes transmitted

def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
  transmit(origin, destination, new Array[Byte](1024 * 10), 0L)


def copy(origin: File, destination: File): IO[Long] = {
  val inIO: IO[InputStream]  = IO(new FileInputStream(origin))
  val outIO:IO[OutputStream] = IO(new FileOutputStream(destination))

  (inIO, outIO)              // Stage 1: Getting resources
    .tupled                  // From (IO[InputStream], IO[OutputStream]) to IO[(InputStream, OutputStream)]
    .bracket{
      case (in, out) =>
        transfer(in, out)    // Stage 2: Using resources (for copying data, in this case)
    } {
      case (in, out) =>      // Stage 3: Freeing resources
        (IO(in.close()), IO(out.close()))
        .tupled              // From (IO[Unit], IO[Unit]) to IO[(Unit, Unit)]
        .void.handleErrorWith(_ => IO.unit)
    }
}
// def copy(origin: File, destination: File): IO[Long] =
//   inputOutputStreams(origin, destination).use { case (in, out) =>
//     transfer(in, out)
//   }


// obviously this isn't actually the problem definition, but it's kinda fun
object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _      <- if(args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
                else IO.unit
      orig = new File(args(0))
      dest = new File(args(1))
      _ <- IO.blocking(dest.exists()).ifM(
        Console[IO].println("Output file exists. Enter yes to continue") >> Console[IO].readLine.map(_ == "yes").ifM(IO.unit, IO.raiseError( new IllegalArgumentException("Aborting"))),
        IO.raiseError(new IllegalArgumentException("Finish execution"))
      )
      _ <- if (orig == dest) IO.raiseError(new IllegalArgumentException("Input and output file names cannot be the same"))
           else IO.unit
      count <- copy(orig, dest)
      _     <- IO.println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
    } yield ExitCode.Success
}


