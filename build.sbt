import Project.project
import sbt.Keys._

organization := "com.pizza"

name := "MeapTrying"

version := "1.0"

lazy val `meaptrying` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc ,
  cache ,
  ws ,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.jooq" % "jooq" % "3.6.0",
  "org.jooq" % "jooq-codegen-maven" % "3.6.0",
  "org.jooq" % "jooq-meta" % "3.6.0",
  "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0",
  specs2 % Test )

val generateJOOQ = taskKey[Seq[File]]("Generate JooQ classes")

val generateJOOQTask = (sourceManaged, dependencyClasspath in Compile,
  runner in Compile, streams) map { (src, cp, r, s) =>
  val outputDir = (src / "jooq").getPath

  toError(r.run("org.jooq.util.GenerationTool", cp.files, Array("conf/chapter.xml"), s.log))

  ((src / "main/generated") ** "*.scala").get

}

generateJOOQ <<= generateJOOQTask

sourceGenerators in Compile <+= generateJOOQTask

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers +=  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

