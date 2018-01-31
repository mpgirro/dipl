package echo.core.model.domain;

import echo.core.model.feed.FeedStatus;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Maximilian Irro
 */
@Entity
@Table(name = "feed")
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Feed implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "url")
    private String url;

    @Column(name = "last_checked")
    private Timestamp lastChecked;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_status")
    private FeedStatus lastStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="podcast_id")
    private Podcast podcast;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
            ", url='" + url + '\'' +
            ", lastChecked=" + lastChecked +
            ", lastStatus=" + lastStatus +
            '}';
    }
}
