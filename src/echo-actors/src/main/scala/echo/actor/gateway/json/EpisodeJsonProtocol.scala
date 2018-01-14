package echo.actor.gateway.json

import echo.core.dto.document.EpisodeDocument
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object EpisodeJsonProtocol extends DefaultJsonProtocol {
    implicit object EpisodeJsonFormat extends RootJsonFormat[EpisodeDocument] {
        def write(p: EpisodeDocument) = JsObject(
            "echoId"      -> JsString(p.getEchoId),
            "title"       -> JsString(p.getTitle),
            "link"        -> JsString(p.getLink),
            "description" -> JsString(p.getDescription),
            "itunesImage" -> JsString(p.getItunesImage)
        )
        def read(value: JsValue) = {
            value.asJsObject.getFields("echoId", "title", "link", "description", "itunesImage") match {
                case Seq(JsString(echoId), JsString(title), JsString(link), JsString(description), JsString(itunesImage)) =>
                    new EpisodeDocument(echoId, title, link, description, itunesImage)
                case _ => throw new DeserializationException("EpisodeDocument expected")
            }
        }
    }
}
