package echo.microservice.catalog.repository;

import echo.core.domain.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed,Long> {

    Feed findOneByEchoId(String echoId);

    List<Feed> findAllByUrl(String url);

    @Query("SELECT DISTINCT feed FROM Feed feed WHERE feed.podcast.echoId = :podcastId")
    List<Feed> findAllByPodcast(@Param("podcastId") String podcastId);

    @Query("SELECT DISTINCT feed FROM Feed feed WHERE feed.url = :url AND feed.podcast.echoId = :podcastId")
    Feed findOneByUrlAndPodcastEchoId(@Param("url") String url,
                                      @Param("podcastId") String podcastId);

    @Query("SELECT count(feed) FROM Feed feed")
    Long countAll();

    @Query("SELECT count(feed) FROM Feed feed WHERE feed.podcast.echoId = :podcastId")
    Long countByPodcast(@Param("podcastId") String podcastId);

}
