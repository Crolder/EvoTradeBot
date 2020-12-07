name := "TradePlatform"

scalaVersion := "2.13.3"

val doobieVersion = "0.9.0"
val canoeVersion = "0.5.1"

libraryDependencies ++= Seq(
    "org.augustjune" %% "canoe" % canoeVersion,
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-h2" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test
)

