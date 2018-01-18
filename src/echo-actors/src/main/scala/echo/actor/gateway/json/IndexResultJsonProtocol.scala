package echo.actor.gateway.json

import java.time.LocalDateTime

import echo.core.dto.IndexResult
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object IndexResultJsonProtocol extends DefaultJsonProtocol {
    implicit object IndexResultJsonFormat extends RootJsonFormat[IndexResult] {
        def write(r: IndexResult) = JsObject(
            "docType"     -> Option(r.getDocType).map(value => JsString(value)).getOrElse(JsNull),
            "echoId"      -> Option(r.getEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "title"       -> Option(r.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"        -> Option(r.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "pubDate"     -> Option(r.getPubDate).map(value => JsString(value.toString)).getOrElse(JsNull),
            "description" -> Option(r.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "itunesImage" -> Option(r.getItunesImage).map(value => JsString(value)).getOrElse(JsNull)
        )
        def read(value: JsValue) = {
            value.asJsObject.getFields("docType", "echoId", "title", "link", "pubDate", "description", "itunesImage") match {
                case Seq(JsString(docType), JsString(echoId), JsString(title), JsString(link), JsString(pubDate), JsString(description), JsString(itunesImage)) =>
                    val result = new IndexResult()
                    result.setDocType(docType)
                    result.setEchoId(echoId)
                    result.setTitle(title)
                    result.setLink(link)
                    result.setPubDate(LocalDateTime.parse(pubDate))
                    result.setDescription(description)
                    result.setItunesImage(itunesImage)
                    result
                    //new IndexResult(docType, echoId, title, link, LocalDateTime.parse(pubDate), description, itunesImage)
                case _ => throw new DeserializationException("IndexResult expected")
            }
        }
    }
}
