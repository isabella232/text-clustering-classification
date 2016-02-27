package com.tpg.infop.batch

import org.rogach.scallop.ScallopConf
import org.rogach.scallop._

/**
  * Created by sayantamd on 27/2/16.
  */
class CmdJobConf(arguments: Seq[String]) extends ScallopConf(arguments) {

  val master = opt(name = "master", short = 'm', descr = "Spark master", default = Some("local"))
  val appName = opt(name = "app", short = 'n', descr = "Spark app name", default = Some("InfoPalace"))
  val k = opt(name = "clusters", short = 'k', descr = "Number of clusters", default = Some(-1))
  val max = opt(name = "max", short = 't', descr = "Maximum number of iterations", default = Some(-1))
  val inputPath = trailArg[String]("input", descr = "Input pattern (can be file globs)")
  val outputPath = trailArg[String]("output", descr = "Output path, will be overwritten if exists")
}
