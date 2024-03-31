ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "dns-server" ,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.0",
      "dev.zio" %% "zio" % "2.0.21",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.0",
      "dnsjava" % "dnsjava" % "3.5.3",
      "org.slf4j" % "slf4j-nop" % "2.0.9",
    ),
  )
