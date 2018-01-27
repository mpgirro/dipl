package echo.core.dto;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class EpisodeDTO implements DTO {

    private String echoId;
    private String docId;

    private String title;
    private String link;
    private LocalDateTime pubDate;
    private String guid;
    private boolean guidIsPermaLink;
    private String description;

    private String itunesImage;
    private String itunesDuration;

    private String websiteData;

    // TODO atom elements

    // TODO itunes elements

    // TODO content:encoded

    // TODO psc:chapters

    // TODO fyyd

    public EpisodeDTO() {

    }

    public EpisodeDTO(String echoId, String title, String link, String description, String itunesImage) {
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
    public LocalDateTime getPubDate() {
        return pubDate;
    }

    @Override
    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public boolean isGuidIsPermaLink() {
        return guidIsPermaLink;
    }

    public void setGuidIsPermaLink(boolean guidIsPermaLink) {
        this.guidIsPermaLink = guidIsPermaLink;
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
    public String getItunesImage() {
        return itunesImage;
    }

    @Override
    public void setItunesImage(String itunesImage) {
        this.itunesImage = itunesImage;
    }

    public String getItunesDuration() {
        return itunesDuration;
    }

    public void setItunesDuration(String itunesDuration) {
        this.itunesDuration = itunesDuration;
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
        return "EpisodeDTO{" +
            "echoId='" + echoId + '\'' +
            ", docId='" + docId + '\'' +
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