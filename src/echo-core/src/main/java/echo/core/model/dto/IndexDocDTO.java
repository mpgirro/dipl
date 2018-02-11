package echo.core.model.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
public class IndexDocDTO {

    private String docType;
    private String echoId;
    private String title;
    private String link;
    private String description;
    private LocalDateTime pubDate;
    private String itunesImage;
    //private Set<String> itunesCategories;
    private String websiteData;

    public IndexDocDTO(){

    }

    public IndexDocDTO(String docType, String echoId, String title, String link, LocalDateTime pubDate, String description, String itunesImage) {
        this.docType = docType;
        this.echoId = echoId;
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.itunesImage = itunesImage;
        //this.itunesCategories = itunesCategories;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
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

    /*
    public Set<String> getItunesCategories() {
        return itunesCategories;
    }

    public void setItunesCategories(Set<String> itunesCategories) {
        this.itunesCategories = itunesCategories;
    }
    */

    public String getWebsiteData() {
        return websiteData;
    }

    public void setWebsiteData(String websiteData) {
        this.websiteData = websiteData;
    }

    @Override
    public String toString() {
        return "IndexDocDTO{" +
            "docType='" + docType + '\'' +
            ", echoId='" + echoId + '\'' +
            ", title='" + title + '\'' +
            ", link='" + link + '\'' +
            ", pubDate=" + pubDate +
            ", description='" + description + '\'' +
            ", itunesImage='" + itunesImage + '\'' +
            //", itunesCategories='" + String.join(", ", itunesCategories) + '\'' +
            ", websiteData='" + websiteData + '\'' +
            '}';
    }
}
