package echo.actor.gateway.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import echo.actor.gateway.{ArrayWrapper, ResultDoc}
import spray.json.{DefaultJsonProtocol, JsonFormat}

/**
  * @author Maximilian Irro
  */

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val resultFormat = jsonFormat5(ResultDoc)
    implicit def arrayWrapper[T: JsonFormat] = jsonFormat1(ArrayWrapper.apply[T])

    implicit val podcastFormat = PodcastJsonProtocol.PodcastJsonFormat
    implicit val episodeFormat = EpisodeJsonProtocol.EpisodeJsonFormat
}
