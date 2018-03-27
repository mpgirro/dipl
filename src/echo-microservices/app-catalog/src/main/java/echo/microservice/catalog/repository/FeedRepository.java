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

    FeedEntity findOneByEchoId(String echoId);

    List<FeedEntity> findAllByUrl(String url);

    @Query("SELECT DISTINCT feed FROM FeedEntity feed WHERE feed.podcast.echoId = :podcastId")
    List<FeedEntity> findAllByPodcast(@Param("podcastId") String podcastId);

    @Query("SELECT DISTINCT feed FROM FeedEntity feed WHERE feed.url = :url AND feed.podcast.echoId = :podcastId")
    FeedEntity findOneByUrlAndPodcastEchoId(@Param("url") String url,
                                      @Param("podcastId") String podcastId);

    @Query("SELECT count(feed) FROM FeedEntity feed")
    Long countAll();

    @Query("SELECT count(feed) FROM FeedEntity feed WHERE feed.podcast.echoId = :podcastId")
    Long countByPodcast(@Param("podcastId") String podcastId);

}
