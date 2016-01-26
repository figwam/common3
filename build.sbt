name := "common"

version := "3.0.1"

scalaVersion := "2.11.7"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "3.0.2",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24",
  "org.webjars" % "bootstrap" % "3.1.1",
  "com.mohiva" %% "play-silhouette-testkit" % "3.0.2" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.webjars.bower" % "font-awsome" % "4.4.0",
  specs2 % Test,
  "com.typesafe.play" %% "play-slick" % "1.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.1",
  "org.postgresql" % "postgresql" % "9.4-1202-jdbc42",
  "com.typesafe.play" %% "play-mailer" % "3.0.1",
  "com.sendgrid" % "sendgrid-java" % "2.2.1",
  cache,
  evolutions,
  filters
)


lazy val common = (project in file(".")).enablePlugins(PlayScala, JavaAppPackaging)

routesGenerator := InjectedRoutesGenerator
