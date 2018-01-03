package alokka.actor.protocol

object SearchProtocol {

  trait SearchMessage

  case class SearchQuery(query : String)

}
