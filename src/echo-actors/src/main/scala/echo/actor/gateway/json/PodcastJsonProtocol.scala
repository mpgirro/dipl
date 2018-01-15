package echo.actor.gateway.json

import echo.core.dto.document.PodcastDocument
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object PodcastJsonProtocol extends DefaultJsonProtocol {
    implicit object PodcastJsonFormat extends RootJsonFormat[PodcastDocument] {
        def write(p: PodcastDocument) = JsObject(
            "echoId"      -> Option(p.getEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "title"       -> Option(p.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"        -> Option(p.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "description" -> Option(p.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "itunesImage" -> Option(p.getItunesImage).map(value => JsString(value)).getOrElse(JsNull)
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
