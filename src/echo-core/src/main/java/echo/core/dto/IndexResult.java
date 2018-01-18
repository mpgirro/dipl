package echo.core.dto;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class IndexResult {

    private String docType;
    private String echoId;
    private String title;
    private String link;
    private LocalDateTime pubDate;
    private String description;
    private String itunesImage;

    public IndexResult(){

    }

    public IndexResult(String docType, String echoId, String title, String link, LocalDateTime pubDate, String description, String itunesImage) {
        this.docType = docType;
        this.echoId = echoId;
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.itunesImage = itunesImage;
    }

    public String getDocType() {
        return this.docType;
    }

    public void setDocType(String docType){
        this.docType = docType;
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

}
