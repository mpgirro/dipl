package echo.actor.gateway.json

import java.time.LocalDateTime

import echo.core.mapper.DateMapper
import echo.core.model.dto.PodcastDTO
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

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
            "itunesImage" -> Option(p.getItunesImage).map(value => JsString(value)).getOrElse(JsNull)
        )
        def read(value: JsValue): PodcastDTO = {
            value.asJsObject.getFields("echoId", "title", "link", "pubDate", "description", "itunesImage") match {
                case Seq(JsString(echoId), JsString(title), JsString(link),  JsString(pubDate), JsString(description), JsString(itunesImage)) =>
                    val podcast = new PodcastDTO()
                    podcast.setEchoId(echoId)
                    podcast.setTitle(title)
                    podcast.setLink(link)
                    podcast.setPubDate(DateMapper.INSTANCE.asLocalDateTime(pubDate))
                    podcast.setDescription(description)
                    podcast.setItunesImage(itunesImage)
                    podcast
                    //new PodcastDTO(echoId, title, link, description, itunesImage)
                case _ => throw DeserializationException("PodcastDTO expected")
            }
        }
    }
}
