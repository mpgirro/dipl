package echo.actor.gateway.json

import echo.actor.gateway.json.ChapterJsonProtocol.ChapterJsonFormat
import echo.core.domain.dto.EpisodeDTO
import echo.core.mapper.DateMapper
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object EpisodeJsonProtocol extends DefaultJsonProtocol {
    implicit object EpisodeJsonFormat extends RootJsonFormat[EpisodeDTO] {
        def write(e: EpisodeDTO) = JsObject(
            "echoId"          -> Option(e.getEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "podcastEchoId"   -> Option(e.getPodcastEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "podcastTitle"    -> Option(e.getPodcastTitle).map(value => JsString(value)).getOrElse(JsNull),
            "title"           -> Option(e.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"            -> Option(e.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "pubDate"         -> Option(e.getPubDate).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "description"     -> Option(e.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "image"           -> Option(e.getImage).map(value => JsString(value)).getOrElse(JsNull),
            "itunesDuration"  -> Option(e.getItunesDuration).map(value => JsString(value)).getOrElse(JsNull),
            "itunesSubtitle"  -> Option(e.getItunesSubtitle).map(value => JsString(value)).getOrElse(JsNull),
            "itunesAuthor"    -> Option(e.getItunesAuthor).map(value => JsString(value)).getOrElse(JsNull),
            "itunesSummary"   -> Option(e.getItunesSummary).map(value => JsString(value)).getOrElse(JsNull),
            "enclosureUrl"    -> Option(e.getEnclosureUrl).map(value => JsString(value)).getOrElse(JsNull),
            "enclosureType"   -> Option(e.getEnclosureType).map(value => JsString(value)).getOrElse(JsNull),
            "enclosureLength" -> Option(e.getEnclosureLength).map(value => JsNumber(value)).getOrElse(JsNull),
            "contentEncoded"  -> Option(e.getContentEncoded).map(value => JsString(value)).getOrElse(JsNull)
        )
        def read(value: JsValue): EpisodeDTO = {
            value.asJsObject.getFields(
                "echoId", "title", "link", "podcastEchoId", "podcastTitle",
                "pubDate", "description", "image", "itunesDuration",
                "itunesSubtitle", "itunesAuthor", "itunesSummary",
                "enclosureLength, chapters") match {
                case Seq(
                    JsString(echoId), JsString(title), JsString(link), JsString(podcastEchoId),
                    JsString(podcastTitle), JsString(pubDate), JsString(description), JsString(image),
                    JsString(itunesDuration), JsString(itunesSubtitle), JsString(itunesAuthor),
                    JsString(itunesSummary), JsNumber(enclosureLength), JsArray(chapters)) =>

                    val episode = new EpisodeDTO()
                    episode.setEchoId(echoId)
                    episode.setPodcastEchoId(podcastEchoId)
                    episode.setPodcastTitle(podcastTitle)
                    episode.setTitle(title)
                    episode.setLink(link)
                    episode.setPubDate(DateMapper.INSTANCE.asLocalDateTime(pubDate))
                    episode.setDescription(description)
                    episode.setImage(image)
                    episode.setItunesDuration(itunesDuration)
                    episode.setItunesSubtitle(itunesSubtitle)
                    episode.setItunesAuthor(itunesAuthor)
                    episode.setItunesSummary(itunesSummary)
                    Option(enclosureLength).foreach(cl => episode.setEnclosureLength(cl.toLong))

                    episode

                case _ => throw DeserializationException("EpisodeDTO expected")
            }
        }
    }
}
