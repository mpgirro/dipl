package echo.core.model.domain;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.hibernate.annotations.Cascade;
//import org.hibernate.annotations.CascadeType;

/**
 * @author Maximilian Irro
 */
@Entity
@Table(name = "podcast",
    indexes = {@Index(name = "idx_podcast_echo_id",  columnList="echo_id", unique = true)})
//@Cacheable(false)
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

    @Column(name = "copyright")
    private String copyright;

    @Column(name = "docs")
    private String docs;

    @Column(name = "managing_editor")
    private String managingEditor;

    @Column(name = "itunes_image")
    private String itunesImage;

    @Column(name = "category")
    @ElementCollection(targetClass=String.class)
    @CollectionTable(
        name="itunes_category",
        joinColumns=@JoinColumn(name="podcast_id")
    )
    private Set<String> itunesCategories;

    @Column(name = "itunes_summary")
    private String itunesSummary;

    @Column(name = "itunes_author")
    private String itunesAuthor;

    @Column(name = "itunes_keywords")
    private String itunesKeywords;

    @Column(name = "itunes_explicit")
    private Boolean itunesExplicit;

    @Column(name = "itunes_block")
    private Boolean itunesBlock;

    @Column(name = "itunes_type")
    private String itunesType;

    @Column(name = "itunes_owner_name")
    private String itunesOwnerName;

    @Column(name = "itunes_owner_email")
    private String itunesOwnerEmail;

    @Column(name = "feedpress_locale")
    private String feedpressLocale;

    @Column(name = "fyyd_verify")
    private String fyydVerify;

    @Column(name = "episode_count")
    private Integer episodeCount;

    @Column(name = "registration_timestamp")
    private Timestamp registrationTimestamp;

    @Column(name = "registration_complete")
    private Boolean registrationComplete;

    @OneToMany(fetch=FetchType.LAZY,
               //cascade = CascadeType.ALL,
               orphanRemoval = true,
               mappedBy="podcast")
//   @Cascade(CascadeType.DELETE)
//    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Episode> episodes = new LinkedHashSet();

    @OneToMany(fetch=FetchType.LAZY,
               //cascade = CascadeType.ALL, // TODO suspected of causing the null elements in getAllPodcasts
               orphanRemoval = true,
               mappedBy="podcast")
//    @Cascade(CascadeType.DELETE)
//    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDocs() {
        return docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public String getManagingEditor() {
        return managingEditor;
    }

    public void setManagingEditor(String managingEditor) {
        this.managingEditor = managingEditor;
    }

    public String getItunesImage() {
        return itunesImage;
    }

    public void setItunesImage(String itunesImage) {
        this.itunesImage = itunesImage;
    }

    public Set<String> getItunesCategories() {
        return itunesCategories;
    }

    public void setItunesCategories(Set<String> itunesCategories) {
        this.itunesCategories = itunesCategories;
    }

    public String getItunesSummary() {
        return itunesSummary;
    }

    public void setItunesSummary(String itunesSummary) {
        this.itunesSummary = itunesSummary;
    }

    public String getItunesAuthor() {
        return itunesAuthor;
    }

    public void setItunesAuthor(String itunesAuthor) {
        this.itunesAuthor = itunesAuthor;
    }

    public String getItunesKeywords() {
        return itunesKeywords;
    }

    public void setItunesKeywords(String itunesKeywords) {
        this.itunesKeywords = itunesKeywords;
    }

    public Boolean getItunesExplicit() {
        return itunesExplicit;
    }

    public void setItunesExplicit(Boolean itunesExplicit) {
        this.itunesExplicit = itunesExplicit;
    }

    public Boolean getItunesBlock() {
        return itunesBlock;
    }

    public void setItunesBlock(Boolean itunesBlock) {
        this.itunesBlock = itunesBlock;
    }

    public String getItunesType() {
        return itunesType;
    }

    public void setItunesType(String itunesType) {
        this.itunesType = itunesType;
    }

    public String getItunesOwnerName() {
        return itunesOwnerName;
    }

    public void setItunesOwnerName(String itunesOwnerName) {
        this.itunesOwnerName = itunesOwnerName;
    }

    public String getItunesOwnerEmail() {
        return itunesOwnerEmail;
    }

    public void setItunesOwnerEmail(String itunesOwnerEmail) {
        this.itunesOwnerEmail = itunesOwnerEmail;
    }

    public String getFeedpressLocale() {
        return feedpressLocale;
    }

    public void setFeedpressLocale(String feedpressLocale) {
        this.feedpressLocale = feedpressLocale;
    }

    public String getFyydVerify() {
        return fyydVerify;
    }

    public void setFyydVerify(String fyydVerify) {
        this.fyydVerify = fyydVerify;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public Timestamp getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(Timestamp registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public Boolean getRegistrationComplete() {
        return registrationComplete;
    }

    public void setRegistrationComplete(Boolean registrationComplete) {
        this.registrationComplete = registrationComplete;
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
            "id=" + id +
            ", echoId='" + echoId + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", description='" + description + '\'' +
            ", pubDate=" + pubDate +
            ", lastBuildDate=" + lastBuildDate +
            ", language='" + language + '\'' +
            ", generator='" + generator + '\'' +
            ", copyright=" + copyright + '\'' +
            ", docs=" + docs + '\'' +
            ", managingEditor=" + managingEditor + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            ", itunesCategories='" + String.join(", ", itunesCategories) + '\'' +
            ", itunesSummary='" + itunesSummary + '\'' +
            ", itunesAuthor='" + itunesAuthor + '\'' +
            ", itunesKeywords='" + itunesKeywords + '\'' +
            ", itunesExplicit=" + itunesExplicit +
            ", itunesBlock=" + itunesBlock +
            ", itunesType='" + itunesType + '\'' +
            ", itunesOwnerName='" + itunesOwnerName + '\'' +
            ", itunesOwnerEmail='" + itunesOwnerEmail + '\'' +
            ", feedpressLocale='" + feedpressLocale + '\'' +
            ", fyydVerify='" + fyydVerify + '\'' +
            ", episodeCount=" + episodeCount +
            ", registrationTimestamp=" + registrationTimestamp +
            ", registrationComplete=" + registrationComplete +
            '}';
    }
}
