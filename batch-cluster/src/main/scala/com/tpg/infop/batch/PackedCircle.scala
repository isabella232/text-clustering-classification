package com.tpg.infop.batch

import scala.collection.mutable.ArrayBuffer

/**
  * Created by sayantamd on 21/1/16.
  */
case class PackedCircle(name: String,
                        size: Double,
                        children: ArrayBuffer[PackedCircle],
                        commonTerms: ArrayBuffer[TermScore],
                        uniqueTerms: ArrayBuffer[TermScore]) {

  def unionCommonTerms(otherCommonTerms: ArrayBuffer[TermScore], maxSize: Int = -1) = {
    this.commonTerms ++= otherCommonTerms
    val distinctCommonTerms = this.commonTerms.distinct.sortBy(_.score)
    this.commonTerms.clear()
    this.commonTerms ++= (if (maxSize > 0) distinctCommonTerms.take(maxSize) else distinctCommonTerms)
  }

  def unionUniqueTerms(otherUniqueTerms: ArrayBuffer[TermScore], maxSize: Int = -1) = {
    this.uniqueTerms ++= otherUniqueTerms
    val distinctUniqueTerms = this.uniqueTerms.distinct.sortWith(_.score > _.score)
    this.uniqueTerms.clear()
    this.uniqueTerms ++= (if (maxSize > 0) distinctUniqueTerms.take(maxSize) else distinctUniqueTerms)
  }
}

