package echo.core.domain.entity;

import echo.core.domain.feed.FeedStatus;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author Maximilian Irro
 */
@Entity
@Table(name = "feed",
    indexes = {@Index(name = "idx_feed_echo_id",  columnList="echo_id", unique = true)})
//@Cacheable(false)
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Feed implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "echo_id")
    private String echoId;

    @Column(name = "url")
    private String url;

    @Column(name = "last_checked")
    private Timestamp lastChecked;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_status")
    private FeedStatus lastStatus;

    @Column(name = "registration_timestamp")
    private Timestamp registrationTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="podcast_id")
    private Podcast podcast;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEchoId() {
        return echoId;
    }

    public void setEchoId(String echoId) {
        this.echoId = echoId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timestamp getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Timestamp lastChecked) {
        this.lastChecked = lastChecked;
    }

    public FeedStatus getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(FeedStatus lastStatus) {
        this.lastStatus = lastStatus;
    }

    public Timestamp getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(Timestamp registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public void setPodcast(Podcast podcast) {
        this.podcast = podcast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Feed feed = (Feed) o;
        if(feed.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, feed.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Feed{" +
            "id=" + id +
            ", echoId='" + echoId + '\'' +
            ", url='" + url + '\'' +
            ", lastChecked=" + lastChecked +
            ", lastStatus=" + lastStatus +
            ", registrationTimestamp=" + registrationTimestamp +
            '}';
    }
}