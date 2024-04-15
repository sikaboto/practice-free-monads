import cats.data.EitherK
import cats.free.Free
import cats.data.Validated
import cats.syntax.validated._
import cats.syntax.flatMap.toFlatMapOps
import cats.FlatMap.ops.toAllFlatMapOps
import cats.Bimonad.ops.toAllBimonadOps
import cats.CommutativeFlatMap.ops.toAllCommutativeFlatMapOps
import cats.CommutativeMonad.ops.toAllCommutativeMonadOps
import cats.Monad.ops.toAllMonadOps
import cats.effect.IO
import cats.{Id, InjectK, ~>}
import scala.io.StdIn.readLine
import scala.collection.mutable.ListBuffer
import cats.syntax.either._
import java.io.File
import java.nio.file.{Paths, Files}

sealed trait Interact[A]
case class Ask[B](prompt: B) extends Interact[String]
case class Tell(msg: String) extends Interact[Unit]

/* Represents persistence operations */
sealed trait DataOp[A]
case class AddCat(a: String) extends DataOp[Unit]
case class GetAllCats() extends DataOp[List[String]]

class Interacts[F[_]](implicit I: InjectK[Interact, F]) {
  def tell(msg: String): Free[F, Unit] = Free.liftInject[F](Tell(msg))
  def ask(prompt: String): Free[F, String] = Free.liftInject[F](Ask(prompt))
}

object Interacts {
  implicit def interacts[F[_]](implicit I: InjectK[Interact, F]): Interacts[F] = new Interacts[F]
}