package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import com.tpg.infop.domain.AnalysisRun
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{Action, Controller}

/**
  * Created by sayantamd on 12/11/15.
  */
@Singleton
class AnalysisRuns @Inject() (system: ActorSystem) extends Controller {

  def create = Action {
    AnalysisRun.invokeAnalysis(system)
    Ok(EmptyContent())
  }
}
