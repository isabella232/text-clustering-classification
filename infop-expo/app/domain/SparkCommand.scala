package domain

import scala.collection.mutable

/**
  * Created by sayantamd on 1/3/16.
  */
case class SparkCommand(inputPath:String, outputPath: String) {

  val options = new mutable.HashMap[String, String]()

  def withOptions(options: Map[String, String]): SparkCommand = {
    this.options ++= options
    this
  }


}
