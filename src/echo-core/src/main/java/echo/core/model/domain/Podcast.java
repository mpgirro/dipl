package echo.core.model.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

//import org.hibernate.annotations.Cascade;
//import org.hibernate.annotations.CascadeType;


/**
 * @author Maximilian Irro
 */
@Entity
@Table(name = "podcast",
    indexes = {@Index(name = "idx_podcast_echo_id",  columnList="echo_id", unique = true)})
public class Podcast implements Serializable {

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

    @Column(name = "last_build_date")
    private Timestamp lastBuildDate;

    @Column(name = "language")
    private String language;

    @Column(name = "generator")
    private String generator;

    @Column(name = "itunes_image")
    private String itunesImage;

    @Column(name = "itunes_category")
    private String itunesCategory;

    @Column(name = "episode_count")
    private int episodeCount;

    @OneToMany(fetch=FetchType.LAZY,
               //cascade = CascadeType.ALL,
               orphanRemoval = true,
               mappedBy="podcast")
//   @Cascade(CascadeType.DELETE)
    private Set<Episode> episodes = new LinkedHashSet();

    @OneToMany(fetch=FetchType.LAZY,
               //cascade = CascadeType.ALL, // TODO suspected of causing the null elements in getAllPodcasts
               orphanRemoval = true,
               mappedBy="podcast")
//    @Cascade(CascadeType.DELETE)
    private Set<Feed> feeds = new LinkedHashSet();

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

    public Timestamp getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(Timestamp lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getItunesImage() {
        return itunesImage;
    }

    public void setItunesImage(String itunesImage) {
        this.itunesImage = itunesImage;
    }

    public String getItunesCategory() {
        return itunesCategory;
    }

    public void setItunesCategory(String itunesCategory) {
        this.itunesCategory = itunesCategory;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public Set<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Set<Episode> episodes) {
        this.episodes = episodes;
    }

    public Set<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(Set<Feed> feeds) {
        this.feeds = feeds;
    }

    public void addEpisode(Episode episode) {
        this.episodes.add(episode);
        this.episodeCount = this.episodes.size();
        episode.setPodcast(this);
    }

    public void removeEpisode(Episode episode) {
        this.episodes.remove(episode);
        this.episodeCount = this.episodes.size();
        episode.setPodcast(null);
    }

    public void addFeed(Feed feed) {
        this.feeds.add(feed);
        feed.setPodcast(this);
    }

    public void removeFeed(Feed feed) {
        this.feeds.remove(feed);
        feed.setPodcast(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Podcast podcast = (Podcast) o;
        if(podcast.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, podcast.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Podcast{" +
            "id=" + id + '\'' +
            ", echoId='" + echoId + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", description='" + description + '\'' +
            ", pubDate=" + pubDate +
            ", lastBuildDate=" + lastBuildDate +
            ", language='" + language + '\'' +
            ", generator='" + generator + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            ", itunesCategory='" + itunesCategory + '\'' +
            ", episodeCount=" + episodeCount +
            '}';
    }
}
