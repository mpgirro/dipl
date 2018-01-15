package echo.core.dto.document;

import java.time.LocalDateTime;

/**
 * @author Maximilian Irro
 */
public class IndexResult {

    private String echoId;
    private String type;
    private String title;
    private String link;
    private LocalDateTime pubDate;
    private String description;
    private String itunesImage;

    public IndexResult(){

    }

    public IndexResult(String echoId, String type, String title, String link, LocalDateTime pubDate, String description, String itunesImage) {
        this.echoId = echoId;
        this.type = type;
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.itunesImage = itunesImage;
    }

    public String getEchoId() {
        return this.echoId;
    }

    public void setEchoId(String echoId) {
        this.echoId = echoId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type){
        this.type = type;
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
