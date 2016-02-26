package com.tpg.infop.batch

import org.scalatest._

import scala.collection.mutable.ArrayBuffer

class PackedCircleSpec extends FlatSpec with Matchers {

  "A PackedCircle" should "add other common terms if its common terms are empty" in {
    val other = ArrayBuffer(TermScore(1, "a"), TermScore(2, "b"))
    val p = PackedCircle("aa", 0, null, ArrayBuffer[TermScore](), ArrayBuffer[TermScore]())
    p.unionCommonTerms(other)
    p.commonTerms should have size 2
  }

  it should "store distinct other common terms" in {
    val other = ArrayBuffer(TermScore(1, "a"), TermScore(2, "a"))
    val p = PackedCircle("aa", 0, null, ArrayBuffer[TermScore](), ArrayBuffer[TermScore]())
    p.unionCommonTerms(other)
    p.commonTerms should have size 1
  }

  it should "perform union with other common terms" in {
    val ct = ArrayBuffer(TermScore(1, "a"), TermScore(2, "a"))
    val other = ArrayBuffer(TermScore(3, "a"), TermScore(4, "a"))
    val p = PackedCircle("aa", 0, null, ct, ArrayBuffer[TermScore]())
    p.unionCommonTerms(other)
    p.commonTerms should have size 1
  }

  it should "limit the union size if maxSize > 0" in {
    val other = ArrayBuffer(TermScore(1, "a"), TermScore(2, "b"))
    val p = PackedCircle("aa", 0, null, ArrayBuffer[TermScore](), ArrayBuffer[TermScore]())
    p.unionCommonTerms(other, 1)
    p.commonTerms should have size 1
  }
}
