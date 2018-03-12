package echo.microservice.catalog.repository;

import echo.core.domain.entity.Podcast;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PodcastRepository extends JpaRepository<Podcast,Long> {

    Podcast findOneByEchoId(String echoId);

    @Query("SELECT DISTINCT podcast FROM Podcast podcast " +
            "LEFT JOIN podcast.feeds feed " +
            "WHERE feed.echoId = :feedId")
    Podcast findOneByFeed(@Param("feedId") String feedId);

    List<Podcast> findByRegistrationCompleteTrue(Pageable pageable);

    @Query("SELECT count(podcast) FROM Podcast podcast")
    Long countAll();

    @Query("SELECT count(podcast) FROM Podcast podcast WHERE podcast.registrationComplete = true")
    Long countAllRegistrationCompleteTrue();

}
