Global / onChangedBuildSource := ReloadOnSourceChanges

val circeVersion = "0.14.1"
val catsVersion = "2.9.0"

lazy val scala213 = "2.13.10"
lazy val scala3 = "3.2.0"

// build.sbt
lazy val hello = project.in(file("."))
  .settings(
    scalaVersion := "3.2.0",
    crossScalaVersions := Seq(scala213, scala3),
    libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core" % catsVersion,
        "org.typelevel" %% "cats-free" % catsVersion,
        "org.typelevel" %% "cats-effect" % "3.4.9",
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion
  ))
