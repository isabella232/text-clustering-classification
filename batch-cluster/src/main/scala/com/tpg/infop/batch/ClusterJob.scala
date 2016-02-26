package com.tpg.infop.batch

import java.io.FileWriter

import com.typesafe.scalalogging.slf4j.Logger
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.{HashingTF, IDF, StopWordsRemover}
import org.apache.spark.mllib.feature
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory

import scala.collection.Map
import scala.collection.mutable.ArrayBuffer

/**
  * Created by sayantamd on 13/1/16.
  */
object ClusterJob {

  val logger = Logger(LoggerFactory.getLogger(ClusterJob.getClass))

  val commonFeatureCount = 10
  val uniqueFeatureCount = 3
  val maxItemsInCluster = 20
  val maxClusters = 1000
  val maxUniqueTerms = 10
  val maxCommonTerms = 10

  def splitDocument(doc: String): (String, String) = {
    if (doc == null) {
      return ("", "")
    }

    val parts = doc.split("\\|{4,4}", 2)
    if (parts.length == 2) (parts(0), parts(1).toLowerCase) else ("", "")
  }

  def sentenceTokenize(content: String): Array[String] = {
    content.split("[\\.!?]").map(_.trim)
  }

  def wordTokenize(sentence: String): Array[String] = {
    sentence.split("\\s+").map(
      w => w.trim.replaceFirst(
        "^\\W*", ""
      ).replaceFirst(
        "\\W*$", ""
      )
    ).filter(w => w.matches("^[a-zA-Z]+$"))
  }

  def sortedVectorTransform(): (Vector => Vector) = {
    (inputVec: Vector) => {
      val inputArray = inputVec.toArray
      val nonZeroList = ArrayBuffer[Double]()
      for (value <- inputArray) {
        if (value > 0.0) {
          nonZeroList += value
        }
      }

      val sortedAsc = nonZeroList.sorted
      Vectors.dense(sortedAsc.toArray)
    }
  }

  def commonFeatureVectorTransform(featureLength: Int): (Vector => Vector) = {
    (sortedAscVec: Vector) => {
      var inputArray = sortedAscVec.toArray
      if (inputArray.length < featureLength) {
        val lastValue = inputArray.last
        inputArray = inputArray.padTo(featureLength, lastValue)
      }

      Vectors.dense(inputArray.take(featureLength))
    }
  }

  def uniqueFeatureScoreTransform(featureLength: Int): (Vector => Double) = {
    (sortedAscVec: Vector) => {
      val inputArray = sortedAscVec.toArray
      val topValues = inputArray.takeRight(featureLength)
      topValues.sum
    }
  }

  def indexedTermTransform(numFeatures: Int): (Seq[String] => Seq[(Int, String)]) = {
    val hashingTF = new feature.HashingTF(numFeatures)
    (termsArray: Seq[String]) => {
      termsArray.distinct.map(term => (hashingTF.indexOf(term), term))
    }
  }

  def sortedTermsTransform(): ((Seq[Row], Vector) => Seq[(Double, String)]) = {
    (indexedTerms: Seq[Row], tfIdfVector: Vector) => {
      indexedTerms.map(row => {
        val index = row.getAs[Int](0)
        val term = row.getAs[String](1)
        (tfIdfVector(index), term)
      }).sortBy(t => t._1)
    }
  }

  def commonTermsTransform(commonFeatureCount: Int): (Seq[(Double, String)] => Seq[(Double, String)]) = {
    (sortedTerms: Seq[(Double, String)]) => {
      sortedTerms.take(commonFeatureCount)
    }
  }

  def uniqueTermsTransform(uniqueFeatureCount: Int): (Seq[(Double, String)] => Seq[(Double, String)]) = {
    (sortedTerms: Seq[(Double, String)]) => {
      sortedTerms.takeRight(uniqueFeatureCount)
    }
  }

  def loadCorpus(sc: SparkContext, inputPath: String): RDD[(String, Array[String])] = {
    val documents = sc.textFile(inputPath)
    val corpus = documents.map(doc => splitDocument(doc)).filter(t => {
      !t._1.isEmpty && !t._2.isEmpty
    }).mapValues(
      content => sentenceTokenize(content)
    ).mapValues(
      sentences => sentences.flatMap(sentence => wordTokenize(sentence))
    )
    corpus
  }

  def addDataTransforms(df: DataFrame): DataFrame = {
    val stopWordRemover = new StopWordsRemover().setInputCol("tokens").setOutputCol("terms")
    val cleanDF = stopWordRemover.transform(df)
    val hashingTF = new HashingTF().setInputCol("terms").setOutputCol("tf")
    val tf = hashingTF.transform(cleanDF)
    val idf = new IDF().setInputCol("tf").setOutputCol("tfidf")
    val idfModel = idf.fit(tf)
    val rescaledData = idfModel.transform(tf)

    val indexedTerms = udf(indexedTermTransform(hashingTF.getNumFeatures))
    val nonZeroSort = udf(sortedVectorTransform())
    val commonFeatures = udf(commonFeatureVectorTransform(commonFeatureCount))
    val uniqueFeatures = udf(uniqueFeatureScoreTransform(uniqueFeatureCount))
    val sortedTerms = udf(sortedTermsTransform())
    val commonTerms = udf(commonTermsTransform(commonFeatureCount))
    val uniqueTerms = udf(uniqueTermsTransform(uniqueFeatureCount))

    val normData = rescaledData.withColumn(
      "indexTerms", indexedTerms(col("terms"))
    ).withColumn(
      "nonZeroSort", nonZeroSort(col("tfidf"))
    ).withColumn(
      "features", commonFeatures(col("nonZeroSort"))
    ).withColumn(
      "size", uniqueFeatures(col("nonZeroSort"))
    ).withColumn(
      "sortedTerms", sortedTerms(col("indexTerms"), col("tfidf"))
    ).withColumn(
      "commonTerms", commonTerms(col("sortedTerms"))
    ).withColumn(
      "uniqueTerms", uniqueTerms(col("sortedTerms"))
    )
    normData
  }

  def clusterTransform(inputDF: DataFrame, k: Int, corpusSize: Long, maxIter: Int): DataFrame = {

    val cachedNormalData = inputDF.cache()

    var optimalK = if (k > 0) k else Math.ceil(Math.sqrt(corpusSize/2.0)).toInt
    if (optimalK > maxClusters) {
      optimalK = maxClusters
    }

    val kmeans = new KMeans().setK(optimalK).setFeaturesCol("features").setPredictionCol("cluster")
    if (maxIter > 0) {
      kmeans.setMaxIter(maxIter)
    }

    val kmModel = kmeans.fit(cachedNormalData)
    kmModel.transform(cachedNormalData)
  }

  def collectAggregateAsMap(clusterModel: DataFrame): Map[Int, ArrayBuffer[PackedCircle]] = {

    clusterModel.select(
      "cluster", "label", "size", "commonTerms", "uniqueTerms"
    ).rdd.map(
      row => (row(0).asInstanceOf[Int], (row(0), row(1), row(2), row(3), row(4)))
    ).aggregateByKey(ArrayBuffer[PackedCircle]())(
      (accumulator, value) => {
        val label = value._2.toString
        val size = value._3.asInstanceOf[Double]
        val commonTerms = value._4.asInstanceOf[Seq[Row]].map(e => TermScore(e.getAs[Double](0), e.getAs[String](1)))
        val uniqueTerms = value._5.asInstanceOf[Seq[Row]].map(e => TermScore(e.getAs[Double](0), e.getAs[String](1)))
        val item = () => PackedCircle(name = label, size, null,
          ArrayBuffer() ++ commonTerms, ArrayBuffer() ++ uniqueTerms
        )
        if (accumulator.size >= maxItemsInCluster) {
          val bottomItem = accumulator.minBy(p => p.size)
          if (bottomItem.size < size) {
            accumulator -= bottomItem
            accumulator += item()
          }
        } else {
          accumulator += item()
        }

        accumulator
      },
      (v1, v2) => {
        v1 ++= v2
      }
    ).collectAsMap()
  }

  def pack(aggregateMap: Map[Int, ArrayBuffer[PackedCircle]], outer: PackedCircle): PackedCircle = {

    aggregateMap.foldLeft(outer)((accumulator, pair: (Int, ArrayBuffer[PackedCircle])) => {
      val (cluster, children) = pair
      val commonTermsInCluster = children.flatMap(e => e.commonTerms).distinct.sortBy(e => e.score).take(maxCommonTerms)
      val uniqueTermsInCluster = children.flatMap(e => e.uniqueTerms).distinct.sortWith(_.score > _.score).take(maxUniqueTerms)
      accumulator.children += PackedCircle(name = "cluster %d".format(cluster), 0.0, children, commonTermsInCluster, uniqueTermsInCluster)
      accumulator.unionCommonTerms(commonTermsInCluster, maxCommonTerms)
      accumulator.unionUniqueTerms(uniqueTermsInCluster, maxUniqueTerms)

      accumulator
    })
  }

  def process(sc: SparkContext, inputPath: String, outputPath: String, sqlCtx: SQLContext = null, k: Int = -1, maxIter: Int = -1): DataFrame = {

    val corpus = loadCorpus(sc, inputPath)
    val corpusSize = corpus.count()
    val sqlContext = if (sqlCtx != null) sqlCtx else new SQLContext(sc)
    import sqlContext.implicits._
    val df = corpus.toDF("label", "tokens")

    val normData = addDataTransforms(df)
    val clusterModel = clusterTransform(normData, k, corpusSize, maxIter)

    val aggMap = collectAggregateAsMap(clusterModel)
    val topics = pack(aggMap, PackedCircle(name = "topics", 0, ArrayBuffer(),
      ArrayBuffer[TermScore](), ArrayBuffer[TermScore]()
    ))

    implicit val formats = Serialization.formats(NoTypeHints)
    val fw = new FileWriter(outputPath)
    fw.write(Serialization.write(topics))
    fw.close()

    clusterModel
  }

  def main(args: Array[String]) {
    val inputPath = args(0)
    val outputPath = args(1)
    val k = if (args.length > 2) Integer.valueOf(args(2)).asInstanceOf[Int] else -1
    val maxIter = if (args.length > 3) Integer.valueOf(args(3)).asInstanceOf[Int] else -1

    val conf = new SparkConf().setMaster("local").setAppName("InfopClusterApp")
    val sc = new SparkContext(conf)

    logger.info("Starting Infop batch job: {}", args)
    process(sc, inputPath, outputPath, null, k, maxIter)
  }
}
