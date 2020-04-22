package dev.stopkran.sbt

import java.nio.file.{Files, Paths}
import sbt._

object United extends AutoPlugin {

  private val defaultSettingsFile = "dependencies.scala"
  // Temporary fix
  private val defaultSettingsDirectory = {
    if (Files.exists(Paths.get("../" + defaultSettingsFile))) {
      ".."
    } else {
      "../.."
    }
  }

  println(s"Settings directory for United-sbt plugin has been set to \'$defaultSettingsDirectory\'")

  private val parsedConf = ConfigParser.parseFile(defaultSettingsDirectory + "/" + defaultSettingsFile)

  val deps: Map[String, ModuleID] = parsedConf._1
  val groups: Map[String, Seq[ModuleID]] = parsedConf._2
  val depsVersions: Map[String, String] = parsedConf._3
  val versions: Map[String, String] = parsedConf._4

}
