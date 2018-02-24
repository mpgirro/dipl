package echo.actor.gateway.json

import java.time.LocalDateTime
import java.util
import java.util.stream.Collectors

import echo.core.domain.dto.IndexDocDTO
import echo.core.mapper.DateMapper
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object IndexResultJsonProtocol extends DefaultJsonProtocol {
    implicit object IndexResultJsonFormat extends RootJsonFormat[IndexDocDTO] {
        def write(r: IndexDocDTO) = JsObject(
            "docType"      -> Option(r.getDocType).map(value => JsString(value)).getOrElse(JsNull),
            "echoId"       -> Option(r.getEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "title"        -> Option(r.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"         -> Option(r.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "pubDate"      -> Option(r.getPubDate).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "description"  -> Option(r.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "podcastTitle" -> Option(r.getPodcastTitle).map(value => JsString(value)).getOrElse(JsNull),
            "itunesImage"  -> Option(r.getItunesImage).map(value => JsString(value)).getOrElse(JsNull)
            //"itunesCategories"  -> Option(r.getItunesCategories).map(value => JsArray(value.asScala.map(c => JsString(c)).toVector)).getOrElse(JsNull)
        )
        def read(value: JsValue): IndexDocDTO = {
            value.asJsObject.getFields("docType", "echoId", "title", "link", "pubDate", "description", "podcastTitle", "itunesImage", "itunesCategories") match {
                case Seq(JsString(docType), JsString(echoId), JsString(title), JsString(link), JsString(pubDate), JsString(podcastTitle), JsString(description), JsString(itunesImage)) =>
                    val result = new IndexDocDTO()
                    result.setDocType(docType)
                    result.setEchoId(echoId)
                    result.setTitle(title)
                    result.setLink(link)
                    result.setPubDate(DateMapper.INSTANCE.asLocalDateTime(pubDate))
                    result.setPodcastTitle(podcastTitle)
                    result.setDescription(description)
                    result.setItunesImage(itunesImage)
                    //result.setItunesCategories(new util.HashSet[String](itunesCategories.map(_.convertTo[String]).asJava))
                    result
                case _ => throw DeserializationException("IndexDocDTO expected")
            }
        }
    }
}
