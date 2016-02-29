package actors

import java.io.{File, FilenameFilter}
import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import play.api.Logger
import domain.ClusterCommand
import play.api.Configuration

/**
  * Created by sayantamd on 29/2/16.
  */

object ClusteringActor {
  def props(out: ActorRef, config: Configuration): Props = Props(new ClusteringActor(out, config))
}


class ClusteringActor(out: ActorRef, config: Configuration) extends Actor {

  val log = Logging(context.system, this)

  override def receive: Receive = {
    case command: ClusterCommand =>
      val totalDocuments = countDocuments(command.category)
      val inputPath = setupProcessDir(command.processCount(totalDocuments), command.category)

      val outputDirPath = new File(config.getString("app.cluster.outputPath").get)
      val outputFileName: String = "%s.json".format(UUID.randomUUID())
      val outputFilePath = new File(outputDirPath, outputFileName).getAbsolutePath

      invokeSpark(inputPath, outputFilePath)
      out ! outputFileName
  }

  def countDocuments(category: String): Int = {
    val inputPath = new File(config.getString("app.cluster.inputPath").get)
    filterInputFiles(category, inputPath).length
  }

  def filterInputFiles(category: String, inputPath: File): Array[File] = {
    new File(inputPath, category).listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {
        name.endsWith(".txt")
      }
    })
  }

  def setupProcessDir(fileCount: Int, category: String): String = {
    val inputPath = new File(config.getString("app.cluster.inputPath").get)
    val processPath = config.getString("app.cluster.processPath").get
    val processDir = new File(processPath)

    // clear the process directory
    processDir.listFiles().foreach((f) => f.delete())

    // copy files from data to process
    filterInputFiles(category, inputPath)
      .take(fileCount)
      .foreach((f) => java.nio.file.Files.copy(f.toPath, new File(processDir, f.getName).toPath))

    processPath
  }

  def invokeSpark(inputPath: String, outputFilePath: String) = {

    val sparkHome = config.getString("app.cluster.spark.home").get
    val sparkMaster = config.getString("app.cluster.spark.master").get
    val sparkJobClass = config.getString("app.cluster.spark.jobClass").get
    val sparkJobAssembly = config.getString("app.cluster.spark.jobAssembly").get

    val cmd = s"$sparkHome/bin/spark-submit --class $sparkJobClass $sparkJobAssembly --master $sparkMaster $inputPath $outputFilePath"
    import sys.process._
    import scala.language.postfixOps
    log.info(cmd)
    Logger.info(cmd)
    cmd!
  }

}
