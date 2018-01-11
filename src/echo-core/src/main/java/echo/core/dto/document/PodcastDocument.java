package echo.core.dto.document;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Maximilian Irro
 */
public class PodcastDocument implements Document {

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

    // TODO atom elements

    // TODO itunes elements

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

    public String getItunesCategory() {
        return itunesCategory;
    }

    public void setItunesCategory(String itunesCategory) {
        this.itunesCategory = itunesCategory;
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

        PodcastDocument d = (PodcastDocument) o;

        if ( ! PodcastDocument.equals(id, PodcastDocument.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    */

    @Override
    public String toString() {
        return "PodcastDocument{" +
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
            '}';
    }
}
