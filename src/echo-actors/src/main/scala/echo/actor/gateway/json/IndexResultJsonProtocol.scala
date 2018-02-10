package echo.actor.gateway.json

import java.time.LocalDateTime

import echo.core.mapper.DateMapper
import echo.core.model.dto.IndexDocDTO
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object IndexResultJsonProtocol extends DefaultJsonProtocol {
    implicit object IndexResultJsonFormat extends RootJsonFormat[IndexDocDTO] {
        def write(r: IndexDocDTO) = JsObject(
            "docType"     -> Option(r.getDocType).map(value => JsString(value)).getOrElse(JsNull),
            "echoId"      -> Option(r.getEchoId).map(value => JsString(value)).getOrElse(JsNull),
            "title"       -> Option(r.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"        -> Option(r.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "pubDate"     -> Option(r.getPubDate).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "description" -> Option(r.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "itunesImage" -> Option(r.getItunesImage).map(value => JsString(value)).getOrElse(JsNull),
            "itunesCategory" -> Option(r.getItunesCategory).map(value => JsString(value)).getOrElse(JsNull)
        )
        def read(value: JsValue): IndexDocDTO = {
            value.asJsObject.getFields("docType", "echoId", "title", "link", "pubDate", "description", "itunesImage", "itunesCategory") match {
                case Seq(JsString(docType), JsString(echoId), JsString(title), JsString(link), JsString(pubDate), JsString(description), JsString(itunesImage), JsString(itunesCategory)) =>
                    val result = new IndexDocDTO()
                    result.setDocType(docType)
                    result.setEchoId(echoId)
                    result.setTitle(title)
                    result.setLink(link)
                    result.setPubDate(DateMapper.INSTANCE.asLocalDateTime(pubDate))
                    result.setDescription(description)
                    result.setItunesImage(itunesImage)
                    result.setItunesCategory(itunesCategory)
                    result
                case _ => throw DeserializationException("IndexDocDTO expected")
            }
        }
    }
}
