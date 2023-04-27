crossScalaVersions := Seq("2.13.5", "3.2.0")

// build.sbt
lazy val hello = project.in(file("."))
  .settings(
    scalaVersion := "3.2.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.4.9"
  )
