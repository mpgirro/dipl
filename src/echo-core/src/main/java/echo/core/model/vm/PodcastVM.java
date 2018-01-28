package echo.core.model.vm;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class PodcastVM {

    private String echoId;

    private String title;
    private String link;
    private String description;
    private LocalDateTime pubDate;
    private LocalDateTime lastBuildDate;
    private String language;

    private String itunesImage;
    private String itunesCategory;

    private int episodeCount;

    public PodcastVM() {

    }

    public PodcastVM(String echoId, String title, String link, String description, String itunesImage) {
        this.echoId = echoId;
        this.title = title;
        this.link = link;
        this.description = description;
        this.itunesImage = itunesImage;
    }

    public String getEchoId() {
        return this.echoId;
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
            "echoId='" + echoId + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", description='" + description + '\'' +
            ", pubDate=" + pubDate +
            ", lastBuildDate=" + lastBuildDate +
            ", language='" + language + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            ", itunesCategory='" + itunesCategory + '\'' +
            ", episodeCount=" + episodeCount + '\'' +
            '}';
    }

}
