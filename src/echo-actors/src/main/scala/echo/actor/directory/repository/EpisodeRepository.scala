package echo.actor.directory.repository

import echo.core.domain.entity.{Episode, Podcast}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait EpisodeRepository extends JpaRepository[Episode, java.lang.Long] {

    def findOneByEchoId(echoId: String): Episode

    def findAllByPodcast(podcast: Podcast): java.util.List[Episode]

    @Query("SELECT DISTINCT episode FROM Episode episode " +
           "LEFT JOIN episode.podcast podcast " +
           "WHERE podcast.echoId = :echoId")
    def findAllByPodcastEchoId(@Param("echoId") echoId: String): java.util.List[Episode]

    @Query("SELECT DISTINCT episode FROM Episode episode " +
        "LEFT JOIN episode.podcast podcast " +
        "WHERE podcast.echoId = :podcastId " +
        "AND episode.guid = :guid")
    def findOneByPodcastAndGuid(@Param("podcastId") podcastId: String, @Param("guid") guid: String): Episode

    @Query("SELECT DISTINCT episode FROM Episode episode " + "" +
           "WHERE episode.enclosureUrl = :enclosureUrl " +
           "AND episode.enclosureLength = :enclosureLength " +
           "AND episode.enclosureType = :enclosureType")
    def findOneByEnlosure(@Param("enclosureUrl") enclosureUrl: String, @Param("enclosureLength") enclosureLength: Long, @Param("enclosureType") enclosureType: String): Episode

}
