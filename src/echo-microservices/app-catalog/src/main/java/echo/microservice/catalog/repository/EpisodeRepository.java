package echo.microservice.catalog.repository;

import echo.core.domain.entity.Episode;
import echo.core.domain.entity.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode,Long> {

    Episode findOneByEchoId(String echoId);

    List<Episode> findAllByPodcast(Podcast podcast);

    @Query("SELECT DISTINCT episode FROM Episode episode WHERE episode.podcast.echoId = :echoId")
    List<Episode> findAllByPodcastEchoId(@Param("echoId") String echoId);

    @Query("SELECT DISTINCT episode FROM Episode episode WHERE episode.podcast.echoId = :podcastId AND episode.guid = :guid")
    List<Episode> findAllByPodcastAndGuid(@Param("podcastId") String podcastId,
                                          @Param("guid") String guid);

    @Query("SELECT DISTINCT episode FROM Episode episode " + "" +
            "WHERE episode.enclosureUrl = :enclosureUrl " +
            "AND episode.enclosureLength = :enclosureLength " +
            "AND episode.enclosureType = :enclosureType")
    Episode findOneByEnlosure(@Param("enclosureUrl") String enclosureUrl,
                              @Param("enclosureLength") Long enclosureLength,
                              @Param("enclosureType") String enclosureTypeg);

    @Query("SELECT count(episode) FROM Episode episode")
    Long countAll();

    @Query("SELECT count(episode) FROM Episode episode WHERE episode.podcast.echoId = :podcastId")
    Long countByPodcast(@Param("podcastId") String podcastId);

}
