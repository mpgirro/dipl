package echo.actor.gateway.json

import java.time.LocalDateTime
import java.util

import echo.core.mapper.DateMapper
import echo.core.model.dto.PodcastDTO
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object PodcastJsonProtocol extends DefaultJsonProtocol {
    implicit object PodcastJsonFormat extends RootJsonFormat[PodcastDTO] {
        def write(p: PodcastDTO) = JsObject(
            "echoId"      -> Option(p.getEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "title"       -> Option(p.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"        -> Option(p.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "pubDate"     -> Option(p.getPubDate).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "description" -> Option(p.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "itunesImage" -> Option(p.getItunesImage).map(value => JsString(value)).getOrElse(JsNull),
            "itunesCategories"  -> Option(p.getItunesCategories).map(value => JsArray(value.asScala.map(c => JsString(c)).toVector)).getOrElse(JsNull)
        )
        def read(value: JsValue): PodcastDTO = {
            value.asJsObject.getFields("echoId", "title", "link", "pubDate", "description", "itunesImage", "itunesCategories") match {
                case Seq(JsString(echoId), JsString(title), JsString(link),  JsString(pubDate), JsString(description), JsString(itunesImage), JsArray(itunesCategories)) =>
                    val podcast = new PodcastDTO()
                    podcast.setEchoId(echoId)
                    podcast.setTitle(title)
                    podcast.setLink(link)
                    podcast.setPubDate(DateMapper.INSTANCE.asLocalDateTime(pubDate))
                    podcast.setDescription(description)
                    podcast.setItunesImage(itunesImage)
                    podcast.setItunesCategories(new util.HashSet[String](itunesCategories.map(_.convertTo[String]).asJava))
                    podcast
                    //new PodcastDTO(echoId, title, link, description, itunesImage)
                case _ => throw DeserializationException("PodcastDTO expected")
            }
        }
    }
}
