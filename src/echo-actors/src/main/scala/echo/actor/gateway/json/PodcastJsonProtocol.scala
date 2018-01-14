package echo.actor.gateway.json

import echo.core.dto.document.PodcastDocument
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object PodcastJsonProtocol extends DefaultJsonProtocol {
    implicit object PodcastJsonFormat extends RootJsonFormat[PodcastDocument] {
        def write(p: PodcastDocument) = JsObject(
            "echoId"      -> JsString(p.getEchoId),
            "title"       -> JsString(p.getTitle),
            "link"        -> JsString(p.getLink),
            "description" -> JsString(p.getDescription),
            "itunesImage" -> JsString(p.getItunesImage)
        )
        def read(value: JsValue) = {
            value.asJsObject.getFields("echoId", "title", "link", "description", "itunesImage") match {
                case Seq(JsString(echoId), JsString(title), JsString(link), JsString(description), JsString(itunesImage)) =>
                    new PodcastDocument(echoId, title, link, description, itunesImage)
                case _ => throw new DeserializationException("PodcastDocument expected")
            }
        }
    }
}
