package echo.actor.index

import echo.core.domain.dto.{IndexDocDTO, ResultWrapperDTO}

/**
  * @author Maximilian Irro
  */
object IndexProtocol {

    trait IndexCommand

    // Crawler/Parser/DirectoryStore -> IndexStore
    case class IndexStoreAddDoc(doc: IndexDocDTO) extends IndexCommand
    case class IndexStoreUpdateDocWebsiteData(exo: String, html: String) extends IndexCommand
    case class IndexStoreUpdateDocImage(exo: String, itunesImage: String) extends IndexCommand
    case class IndexStoreUpdateDocLink(exo: String, newLink: String) extends IndexCommand

    // IndexStore -> IndexStore
    case class CommitIndex() extends IndexCommand


    trait IndexQuery

    // Searcher -> IndexStore
    case class SearchIndex(query: String, page: Int, size: Int) extends IndexQuery


    trait IndexResult

    // IndexStore -> Searcher
    case class IndexResultsFound(query: String, results: ResultWrapperDTO) extends IndexResult
    case class NoIndexResultsFound(query: String) extends IndexResult

}
