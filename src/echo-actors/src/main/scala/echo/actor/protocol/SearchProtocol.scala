package echo.actor.protocol

object SearchProtocol {

    trait SearchMessage

    case class SearchQuery(query: String)

    trait SearchReply

    case class SearchResultsFound(query: String, results: Array[String])
    case class NoResultsFound(query: String)

}
