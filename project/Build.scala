import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "ticketex"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq( // Add your project dependencies here,
     "org.hectorclient" % "hector-core" % "1.1-1"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(

  )
}
