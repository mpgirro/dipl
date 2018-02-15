package echo.actor.gateway.json

import java.time.LocalDateTime

import echo.core.mapper.DateMapper
import echo.core.model.dto.EpisodeDTO
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object EpisodeJsonProtocol extends DefaultJsonProtocol {
    implicit object EpisodeJsonFormat extends RootJsonFormat[EpisodeDTO] {
        def write(e: EpisodeDTO) = JsObject(
            "echoId"         -> Option(e.getEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "title"          -> Option(e.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"           -> Option(e.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "pubDate"        -> Option(e.getPubDate).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "description"    -> Option(e.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "itunesImage"    -> Option(e.getItunesImage).map(value => JsString(value)).getOrElse(JsNull),
            "itunesDuration" -> Option(e.getItunesDuration).map(value => JsString(value)).getOrElse(JsNull),
            "enclosureUrl"   -> Option(e.getEnclosureUrl).map(value => JsString(value)).getOrElse(JsNull),
            "enclosureType"  -> Option(e.getEnclosureType).map(value => JsString(value)).getOrElse(JsNull),
            "contentEncoded" -> Option(e.getContentEncoded).map(value => JsString(value)).getOrElse(JsNull)
        )
        def read(value: JsValue): EpisodeDTO = {
            value.asJsObject.getFields("echoId", "title", "link", "pubDate", "description", "itunesImage", "itunesDuration") match {
                case Seq(JsString(echoId), JsString(title), JsString(link),  JsString(pubDate), JsString(description), JsString(itunesImage), JsString(itunesDuration)) =>
                    val episode = new EpisodeDTO()
                    episode.setEchoId(echoId)
                    episode.setTitle(title)
                    episode.setLink(link)
                    episode.setPubDate(DateMapper.INSTANCE.asLocalDateTime(pubDate))
                    episode.setDescription(description)
                    episode.setItunesImage(itunesImage)
                    episode.setItunesDuration(itunesDuration)
                    episode
                    //new EpisodeDTO(echoId, title, link, description, itunesImage)
                case _ => throw DeserializationException("EpisodeDTO expected")
            }
        }
    }
}
