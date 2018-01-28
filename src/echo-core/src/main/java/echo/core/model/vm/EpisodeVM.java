package echo.core.model.vm;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class EpisodeVM {

    private String echoId;

    private String title;
    private String link;
    private LocalDateTime pubDate;
    private String description;

    private String itunesImage;
    private String itunesDuration;

    public EpisodeVM() {

    }

    public EpisodeVM(String echoId, String title, String link, String description, String itunesImage) {
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

    public LocalDateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    /* TODO do I need these?
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EpisodeDTO d = (EpisodeDTO) o;

        if ( ! EpisodeDTO.equals(id, EpisodeDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    */

    @Override
    public String toString() {
        return "EpisodeVM{" +
            "echoId='" + echoId + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", pubDate=" + pubDate +
            ", description='" + description + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            ", itunesDuration='" + itunesDuration + '\'' +
            '}';
    }

}
