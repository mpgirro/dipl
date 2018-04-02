package echo.microservice.catalog.repository;

import echo.core.domain.entity.EpisodeEntity;
import echo.core.domain.entity.PodcastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface EpisodeRepository extends JpaRepository<EpisodeEntity,Long> {

    EpisodeEntity findOneByExo(String exo);

    List<EpisodeEntity> findAllByPodcast(PodcastEntity podcast);

    @Query("SELECT DISTINCT episode FROM EpisodeEntity episode WHERE episode.podcast.exo = :exo")
    List<EpisodeEntity> findAllByPodcastExo(@Param("exo") String exo);

    @Query("SELECT DISTINCT episode FROM EpisodeEntity episode WHERE episode.podcast.exo = :podcastExo AND episode.guid = :guid")
    List<EpisodeEntity> findAllByPodcastAndGuid(@Param("podcastExo") String podcastExo,
                                                @Param("guid") String guid);

    @Query("SELECT DISTINCT episode FROM EpisodeEntity episode " + "" +
            "WHERE episode.enclosureUrl = :enclosureUrl " +
            "AND episode.enclosureLength = :enclosureLength " +
            "AND episode.enclosureType = :enclosureType")
    EpisodeEntity findOneByEnlosure(@Param("enclosureUrl") String enclosureUrl,
                                    @Param("enclosureLength") Long enclosureLength,
                                    @Param("enclosureType") String enclosureTypeg);

    @Query("SELECT count(episode) FROM EpisodeEntity episode")
    Long countAll();

    @Query("SELECT count(episode) FROM EpisodeEntity episode WHERE episode.podcast.exo = :podcastExo")
    Long countByPodcast(@Param("podcastExo") String podcastExo);

}
