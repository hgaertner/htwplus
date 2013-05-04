import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "sandbox"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaJpa,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "org.hibernate" % "hibernate-entitymanager" % "4.1.5.Final"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here 
		  ebeanEnabled := false       
  )

}
