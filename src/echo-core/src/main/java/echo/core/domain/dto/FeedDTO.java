package echo.core.domain.dto;

import echo.core.domain.feed.FeedStatus;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class FeedDTO implements EntityDTO {

    private Long id;
    private String echoId;
    private Long podcastId;

    // TODO save MIME type of feed
    // TODO title

    private String url;
    private LocalDateTime lastChecked;
    private FeedStatus lastStatus;

    private LocalDateTime registrationTimestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getEchoId() {
        return this.echoId;
    }

    @Override
    public void setEchoId(String echoId) {
        this.echoId = echoId;
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

    public LocalDateTime getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(LocalDateTime registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedDTO that = (FeedDTO) o;

        return echoId.equals(that.echoId);
    }

    @Override
    public int hashCode() {
        return echoId.hashCode();
    }

    @Override
    public String toString() {
        return "FeedDTO{\n" +
            "\tid=" + id + ",\n" +
            "\techoId='" + echoId + "\',\n" +
            "\tpodcastId=" + podcastId + ",\n" +
            "\turl='" + url + "\',\n" +
            "\tlastChecked=" + lastChecked + ",\n" +
            "\tlastStatus='" + lastStatus + "\',\n" +
            "\tregistrationTimestamp=" + registrationTimestamp + ",\n" +
            '}';
    }
}
