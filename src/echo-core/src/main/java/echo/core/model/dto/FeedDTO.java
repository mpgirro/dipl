package echo.core.model.dto;

import echo.core.model.feed.FeedStatus;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author Maximilian Irro
 */
public class FeedDTO {

    private Long id;
    private Long podcastId;

    private String url;
    private LocalDateTime lastChecked;
    private FeedStatus lastStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(Long podcastId) {
        this.podcastId = podcastId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    public FeedStatus getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(FeedStatus lastStatus) {
        this.lastStatus = lastStatus;
    }

    @Override
    public String toString() {
        return "FeedDTO{" +
            "id='" + id +
            ", podcastId=" + podcastId +
            ", url='" + url +
            ", lastChecked=" + lastChecked +
            ", lastStatus=" + lastStatus +
            '}';
    }
}
