package echo.core.model.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
public class PodcastDTO implements DTO {

    private Long id;
    private String echoId;

    private String title;
    private String link;
    private String description;
    private LocalDateTime pubDate;
    private LocalDateTime lastBuildDate;
    private String language;
    private String generator;

    private String itunesImage;
    private Set<String> itunesCategories;

    private int episodeCount;

    private String websiteData;

    // TODO atom elements

    // TODO itunes elements

    public PodcastDTO() {

    }

    public PodcastDTO(String echoId, String title, String link, String description, String itunesImage) {
        this.echoId = echoId;
        this.title = title;
        this.link = link;
        this.description = description;
        this.itunesImage = itunesImage;
    }

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

    public LocalDateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }

    public LocalDateTime getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(LocalDateTime lastBuildDate) {
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

    public Set<String> getItunesCategories() {
        return itunesCategories;
    }

    public void setItunesCategories(Set<String> itunesCategories) {
        this.itunesCategories = itunesCategories;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getWebsiteData() {
        return websiteData;
    }

    public void setWebsiteData(String websiteData) {
        this.websiteData = websiteData;
    }

    /* TODO do I need these?
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PodcastDTO d = (PodcastDTO) o;

        if ( ! PodcastDTO.equals(id, PodcastDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    */

    @Override
    public String toString() {
        return "PodcastDTO{" +
            "id=" + id +
            ", echoId='" + echoId +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", description='" + description + '\'' +
            ", pubDate=" + pubDate +
            ", lastBuildDate=" + lastBuildDate +
            ", language='" + language + '\'' +
            ", generator='" + generator + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            ", itunesCategories='" + String.join(", ", itunesCategories) + '\'' +
            ", episodeCount=" + episodeCount +
            '}';
    }
}
