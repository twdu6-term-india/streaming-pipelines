
val sparkVersion = "2.3.0"

lazy val root = (project in file(".")).

  settings(
    inThisBuild(List(
      organization := "com.tw",
      scalaVersion := "2.11.8",
      version := "0.0.1"
    )),

    name := "tw-e2e",

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "org.apache.hadoop" % "hadoop-common" % "2.6.0",
      "org.apache.commons" % "commons-io" % "1.3.2",
      "org.apache.hadoop" % "hadoop-hdfs" % "2.6.0"
    )
  )