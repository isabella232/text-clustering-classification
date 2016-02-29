package domain

/**
  * Created by sayantamd on 29/2/16.
  */
case class ClusterCommand(category: String, dayCount: Int, totalDays: Int) {

  def processCount(totalCount: Int): Int = {
    if (dayCount == totalDays) {
      totalCount
    } else {
      totalCount * dayCount / totalDays
    }
  }
}
