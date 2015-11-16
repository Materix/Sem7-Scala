name := """AuctionHouse"""

version := "1.0"

scalaVersion := "2.11.7"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"
  

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.0" withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % "test" withSources() withJavadoc(),
  "org.scalatest" %% "scalatest" % "2.2.4" % "test" withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-persistence" % "2.4.0" withSources() withJavadoc(),
  "com.github.ironfish" %% "akka-persistence-mongo-casbah"  % "0.7.6" % "compile",
  "com.github.scullxbones" %% "akka-persistence-mongo-casbah" % "1.0.9",
  "pl.project13.scala" %% "akka-persistence-hbase" % "0.4.0",
  "org.mongodb" %% "casbah" % "3.0.0",
  "com.geteventstore" %% "akka-persistence-eventstore" % "2.1.0",
  "org.iq80.leveldb"            % "leveldb"          % "0.7" withSources() withJavadoc(),
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8" withSources() withJavadoc()
)
