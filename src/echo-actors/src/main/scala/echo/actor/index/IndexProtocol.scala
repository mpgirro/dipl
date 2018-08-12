package echo.actor.index

import com.google.common.collect.ImmutableList
import echo.core.benchmark.rtt.RoundTripTime
import echo.core.domain.dto.{IndexDocDTO, ResultWrapperDTO}

/**
  * @author Maximilian Irro
  */
object IndexProtocol {

    trait IndexEvent

    // Crawler/Parser/CatalogStore -> IndexStore
    case class AddDocIndexEvent(doc: IndexDocDTO, rtt: RoundTripTime) extends IndexEvent
    case class UpdateDocWebsiteDataIndexEvent(exo: String, html: String) extends IndexEvent
    case class UpdateDocImageIndexEvent(exo: String, image: String) extends IndexEvent
    case class UpdateDocLinkIndexEvent(exo: String, newLink: String) extends IndexEvent


    trait IndexCommand

    // IndexStore -> IndexStore
    case class CommitIndex() extends IndexCommand


    trait IndexQuery

    // Searcher -> IndexStore
    case class SearchIndex(query: String, page: Int, size: Int, rtt: RoundTripTime) extends IndexQuery


    trait IndexQueryResult

    // IndexStore -> Searcher
    case class IndexResultsFound(query: String, results: ResultWrapperDTO, rtt: RoundTripTime) extends IndexQueryResult
    case class NoIndexResultsFound(query: String, rtt: RoundTripTime) extends IndexQueryResult

}
