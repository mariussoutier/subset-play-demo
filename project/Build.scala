import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "subset-test"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "org.mongodb" % "casbah-core_2.9.1" % "2.4.1",
      "com.osinka.subset" %% "subset" % "1.0.0"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      templatesImport += "org.bson.types.ObjectId",
      routesImport += "util.Binders._"
    )

}
