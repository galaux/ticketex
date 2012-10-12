import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "ticketex"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
     "org.hectorclient"     % "hector-core" % "1.1-1",
     "com.netflix.astyanax" % "astyanax"    % "1.0.6"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(

  )
}
