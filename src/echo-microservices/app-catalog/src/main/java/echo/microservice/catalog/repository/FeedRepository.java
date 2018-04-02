package echo.microservice.catalog.repository;

import echo.core.domain.entity.FeedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Maximilian Irro
 */
public interface FeedRepository extends JpaRepository<FeedEntity,Long> {

    FeedEntity findOneByExo(String echoId);

    List<FeedEntity> findAllByUrl(String url);

    @Query("SELECT DISTINCT feed FROM FeedEntity feed WHERE feed.podcast.exo = :podcastExo")
    List<FeedEntity> findAllByPodcast(@Param("podcastExo") String podcastExo);

    @Query("SELECT DISTINCT feed FROM FeedEntity feed WHERE feed.url = :url AND feed.podcast.exo = :podcastExo")
    FeedEntity findOneByUrlAndPodcastExo(@Param("url") String url,
                                         @Param("podcastExo") String podcastExo);

    @Query("SELECT count(feed) FROM FeedEntity feed")
    Long countAll();

    @Query("SELECT count(feed) FROM FeedEntity feed WHERE feed.podcast.exo = :podcastExo")
    Long countByPodcast(@Param("podcastExo") String podcastExo);

}
