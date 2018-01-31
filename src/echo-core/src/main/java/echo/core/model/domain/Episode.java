package echo.core.model.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Maximilian Irro
 */
@Entity
@Table(name = "episode",
    indexes = {@Index(name = "idx_episode_echo_id",  columnList="echo_id", unique = true)})
public class Episode implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "echo_id")
    private String echoId;

    @Column(name = "title")
    private String title;

    @Column(name = "link")
    private String link;

    @Column(name = "description")
    private String description;

    @Column(name = "pub_date")
    private Timestamp pubDate;

    @Column(name = "guid")
    private String guid;

    @Column(name = "guid_is_permalink")
    private Boolean guidIsPermaLink;

    @Column(name = "itunes_image")
    private String itunesImage;

    @Column(name = "itunes_duration")
    private String itunesDuration;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getPubDate() {
        return pubDate;
    }

    public void setPubDate(Timestamp pubDate) {
        this.pubDate = pubDate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Boolean getGuidIsPermaLink() {
        return guidIsPermaLink;
    }

    public void setGuidIsPermaLink(Boolean guidIsPermaLink) {
        this.guidIsPermaLink = guidIsPermaLink;
    }

    public String getItunesImage() {
        return itunesImage;
    }

    public void setItunesImage(String itunesImage) {
        this.itunesImage = itunesImage;
    }

    public String getItunesDuration() {
        return itunesDuration;
    }

    public void setItunesDuration(String itunesDuration) {
        this.itunesDuration = itunesDuration;
    }

    public Podcast getPodcast() {
        return podcast;
    }

    public void setPodcast(Podcast podcast) {
        this.podcast = podcast;
    }

    @Override
    public String toString() {
        return "Episode{" +
            "id=" + id + '\'' +
            ", echoId='" + echoId + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", pubDate=" + pubDate +
            ", guid='" + guid + '\'' +
            ", guidIsPermaLink=" + guidIsPermaLink +
            ", description='" + description + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            ", itunesDuration='" + itunesDuration + '\'' +
            '}';
    }
}
