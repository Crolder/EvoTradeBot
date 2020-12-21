name := "TradePlatform"

scalaVersion := "2.13.3"

mainClass in (Compile, run) := Some("main.scala.Main")

val doobieVersion = "0.9.0"
val canoeVersion = "0.5.1"
val scalaTestVersion = "3.1.0.0-RC2"
val catsScalacheckVersion = "0.2.0"

libraryDependencies ++= Seq(
    "org.augustjune" %% "canoe" % canoeVersion,
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-h2" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestVersion % Test,
    "org.scalatestplus" %% "selenium-2-45" % scalaTestVersion % Test,
    "io.chrisdavenport" %% "cats-scalacheck" % catsScalacheckVersion % Test,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestVersion % Test,
)