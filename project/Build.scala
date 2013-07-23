import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "htwplus"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaJpa,
    anorm,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.hibernate" % "hibernate-entitymanager" % "4.1.5.Final",
    //elastic search module
    //"com.clever-age" % "play2-elasticsearch" % "0.6-SNAPSHOT",
    "org.hibernate" % "hibernate-search" % "4.3.0.Final",
    "org.hibernate" % "hibernate-annotations" % "3.5.6-Final",
    "org.hibernate" % "hibernate-core" % "4.2.3.Final")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here 
    ebeanEnabled := false,

    resolvers += Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns))

}
