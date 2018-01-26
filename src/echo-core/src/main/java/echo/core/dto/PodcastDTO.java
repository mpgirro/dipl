package echo.core.dto;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class PodcastDTO implements DTO {

    private String echoId;
    private String docId;

    private String title;
    private String link;
    private String description;
    private LocalDateTime pubDate;
    private LocalDateTime lastBuildDate;
    private String language;
    private String generator;

    private String itunesImage;
    private String itunesCategory;

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

    @Override
    public String getEchoId() {
        return this.echoId;
    }

    @Override
    public void setEchoId(String echoId) {
        this.echoId = echoId;
    }

    @Override
    public String getDocId(){
        return this.docId;
    }

    @Override
    public void setDocId(String docId){
        this.docId = docId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public LocalDateTime getPubDate() {
        return pubDate;
    }

    @Override
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

    @Override
    public String getItunesImage() {
        return itunesImage;
    }

    @Override
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

    @Override
    public String getWebsiteData() {
        return websiteData;
    }

    @Override
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
            "echoId='" + echoId + '\'' +
            ", docId='" + docId + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", description='" + description + '\'' +
            ", pubDate=" + pubDate +
            ", lastBuildDate=" + lastBuildDate +
            ", language='" + language + '\'' +
            ", generator='" + generator + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            ", itunesCategory='" + itunesCategory + '\'' +
            ", episodeCount=" + episodeCount + '\'' +
            '}';
    }
}
