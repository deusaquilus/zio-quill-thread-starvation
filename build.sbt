lazy val root = project in file(".")

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "1.4.199",
  "io.getquill" %% "quill-jdbc-zio" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.5",
  "org.postgresql"          %  "postgresql"              % "42.2.18"
)
