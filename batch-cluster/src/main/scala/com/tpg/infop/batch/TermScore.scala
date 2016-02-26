package com.tpg.infop.batch

/**
  * Created by sayantamd on 24/1/16.
  */
case class TermScore(score: Double, term: String) {

  override def hashCode(): Int = {
    term.hashCode
  }

  override def equals(obj: scala.Any): Boolean = {

    if (obj.asInstanceOf[scala.AnyRef].eq(this)) return true
    if (!obj.isInstanceOf[TermScore]) return false

    val other = obj.asInstanceOf[TermScore]
    if (this.term == null) {
      if (other.term != null) return false
    }

    this.term.equals(other.term)
  }
}
