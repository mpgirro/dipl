package echo.actor.gateway.json

import echo.core.domain.feed.ChapterDTO
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object ChapterJsonProtocol extends DefaultJsonProtocol {
    implicit object ChapterJsonFormat extends RootJsonFormat[ChapterDTO] {
        def write(c: ChapterDTO) = JsObject(
            "start" -> Option(c.getStart).map(value => JsString(value)).getOrElse(JsNull),
            "title" -> Option(c.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "href" -> Option(c.getHref).map(value => JsString(value)).getOrElse(JsNull),
            "image" -> Option(c.getImage).map(value => JsString(value)).getOrElse(JsNull)
        )
        def read(value: JsValue): ChapterDTO = {
            value.asJsObject.getFields("start", "title", "href", "image") match {
                case Seq(JsString(start), JsString(title), JsString(href), JsString(image)) =>

                    val chapter = new ChapterDTO()
                    chapter.setStart(start)
                    chapter.setTitle(title)
                    chapter.setHref(href)
                    chapter.setImage(image)
                    chapter

                case _ => throw DeserializationException("ChapterDTO expected")
            }
        }
    }
}
