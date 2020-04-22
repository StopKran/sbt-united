package dev.stopkran.sbt

import sbt._


object ConfigParser{

  def compilePackage(packageOrg: String, packageName: String, packageVersion: String, packageOpts: Iterable[String], isScalaPackage: Boolean) = {

    val compiledWithoutOptions = if (isScalaPackage){
      packageOrg %% packageName % packageVersion
    } else {
      packageOrg % packageName % packageVersion
    }

    val compiled = packageOpts.foldLeft(compiledWithoutOptions){ case (moduleID: ModuleID, opt: String) => moduleID % opt}

    compiled
  }

  def parse(str: String) = {

    val lines = str.split('\n')
    val packages = lines.filter(_.contains("%"))
    val groupLines = lines.filter(_.contains("Seq"))
    val versionLines = lines.filter(s => s.contains("val") && !s.contains("Seq") && !s.contains("%") )

    val parsedPackages = packages.map{line =>

      val Array(localNameStr,  packageStr) = line.split('=')
      val localName = localNameStr.replace("val", "").trim

      val (packageOrgStr, packageAndVerStr) = packageStr.splitAt(packageStr.indexOfSlice("%"))
      val packageOrg = packageOrgStr.replace('\"', ' ').trim

      val splited = packageAndVerStr.replace("%%", "%").split("%")
      val isScalaPackage = packageAndVerStr.contains("%%")
      val packageName = splited(1).replace('\"', ' ').trim
      val packageVersion = splited(2).replace('\"', ' ').trim

      val packageOpts = splited.drop(3).map(_.replace('\"', ' ').trim)

      localName -> compilePackage(packageOrg, packageName, packageVersion, packageOpts, isScalaPackage)

      (localName, packageOrg, packageName, packageVersion, packageOpts, isScalaPackage)
    }

    val packageMap = parsedPackages.map{ case (localName, packageOrg, packageName, packageVersion, packageOpts, isScalaPackage) =>
      localName -> compilePackage(packageOrg, packageName, packageVersion, packageOpts, isScalaPackage)
    }.toMap

    val depVersions = parsedPackages.map{ case (localName, _, _, packageVersion, _, _) =>
        localName -> packageVersion
    }.toMap

    val versions = versionLines.map{line =>
      val Array(localNameStr, versionStr) = line.split('=')
      val localName = localNameStr.replace("val", "").trim
      val version = versionStr.replace("\n", "").replace('\"', ' ').trim
      localName -> version
    }.toMap


    val groups = groupLines.map{line =>
      val groupName = line.splitAt(line.indexOf('='))._1.replace("val", " ").trim
      val strPackagesInGroup = line.drop(line.indexOfSlice("(")+1).replace(")", "").split(',').map(_.trim)

      groupName -> strPackagesInGroup.map(name => packageMap(name)).toSeq
    }.toMap




    packageMap.foreach(p => println(p))
    groups.foreach(g => println(g))
    (packageMap, groups, depVersions, versions)
  }

  def parseFile(fileName: String) = {
    val source = scala.io.Source.fromFile(fileName)

    val content = source.mkString
    source.close()

    parse(content)
  }


}
