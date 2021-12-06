name := "Simple Project"

version := "1.0"

scalaVersion := "2.13.7"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-sql_2.13" % "3.2.0",
  "com.databricks" %% "spark-xml" % "0.14.0")
