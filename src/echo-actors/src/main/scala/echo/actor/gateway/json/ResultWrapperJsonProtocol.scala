package echo.actor.gateway.json

import echo.actor.gateway.json.IndexResultJsonProtocol.IndexResultJsonFormat
import echo.core.model.dto.{IndexResult, ResultWrapperDTO}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import spray.json.CollectionFormats
import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object ResultWrapperJsonProtocol extends DefaultJsonProtocol {
    implicit object ResultWrapperJsonFormat extends RootJsonFormat[ResultWrapperDTO] {
        def write(rw: ResultWrapperDTO) = JsObject(
            "currPage"  -> Option(rw.getCurrPage).map(value => JsNumber(value)).getOrElse(JsNull),
            "maxPage"   -> Option(rw.getMaxPage).map(value => JsNumber(value)).getOrElse(JsNull),
            "totalHits" -> Option(rw.getTotalHits).map(value => JsNumber(value)).getOrElse(JsNull),
            "results"   -> Option(rw.getResults).map(value => JsArray(value.map(r => IndexResultJsonFormat.write(r)).toVector)).getOrElse(JsNull)
        )
        def read(value: JsValue): ResultWrapperDTO = {
            value.asJsObject.getFields("currPage", "maxPage", "totalHits", "results") match {
                case Seq(JsNumber(currPage), JsNumber(maxPage), JsNumber(totalHits),  JsArray(results)) =>
                    val resultWrapper = new ResultWrapperDTO
                    resultWrapper.setCurrPage(currPage.toInt)
                    resultWrapper.setMaxPage(maxPage.toInt)
                    resultWrapper.setTotalHits(totalHits.toInt)

                    val foo: Array[IndexResult] = results.map(_.convertTo[IndexResult]).to[Array]
                    val bar: java.util.List[IndexResult] = seqAsJavaList(foo)


                    resultWrapper.setResults(foo) // seqAsJavaList()   results.map(_.convertTo[IndexResult]).to[Array[IndexResult]]
                    resultWrapper
                //new PodcastDTO(echoId, title, link, description, itunesImage)
                case _ => throw DeserializationException("ResultWrapperDTO expected")
            }
        }
    }
}
