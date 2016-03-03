package util

import domain.SparkCommand
import play.api.{Configuration, Logger}

/**
  * Created by sayantamd on 1/3/16.
  */
object SparkFacade {

  def invokeSpark(config: Configuration, command: SparkCommand) = {

    val inputPath = command.inputPath
    val outputFilePath = command.outputPath

    val sparkHome = config.getString("home").get
    val sparkMaster = config.getString("master").get
    val sparkJobClass = config.getString("jobClass").get
    val sparkJobAssembly = config.getString("jobAssembly").get
    val assemblyOptions = command.options.foldLeft("") { case (z, (k, v)) =>
      z.concat(s" --$k $v")
    }

    val cmd = s"$sparkHome/bin/spark-submit --class $sparkJobClass $sparkJobAssembly --master $sparkMaster $assemblyOptions $inputPath $outputFilePath"
    Logger.info(cmd)
    import sys.process._
    import scala.language.postfixOps
    cmd!
  }

}
