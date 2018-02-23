package echo.core.domain.dto;

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
    private String itunesAuthor;
    private String itunesSummary;
    //private Set<String> itunesCategories;
    private String contentEncoded;
    private String websiteData;
    private String chapterMarks;

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

    public String getContentEncoded() {
        return contentEncoded;
    }

    public void setContentEncoded(String contentEncoded) {
        this.contentEncoded = contentEncoded;
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

    public String getItunesAuthor() {
        return itunesAuthor;
    }

    public void setItunesAuthor(String itunesAuthor) {
        this.itunesAuthor = itunesAuthor;
    }

    public String getItunesSummary() {
        return itunesSummary;
    }

    public void setItunesSummary(String itunesSummary) {
        this.itunesSummary = itunesSummary;
    }

    public String getChapterMarks() {
        return chapterMarks;
    }

    public void setChapterMarks(String chapterMarks) {
        this.chapterMarks = chapterMarks;
    }

    @Override
    public String toString() {
        return "IndexDocDTO{\n" +
            "\tdocType='" + docType + "\',\n" +
            "\techoId='" + echoId + "\',\n" +
            "\ttitle='" + title + "\',\n" +
            "\tlink='" + link + "\',\n" +
            "\tpubDate=" + pubDate + ",\n" +
            "\tdescription='" + description + "\',\n" +
            "\titunesImage='" + itunesImage + "\',\n" +
            "\titunesAuthor='" + itunesAuthor + "\',\n" +
            "\titunesSummary='" + itunesSummary + "\',\n" +
            "\tchapterMarks='" + chapterMarks + "\',\n" +
            "\tcontentEncoded='" + (contentEncoded==null ? "null" : "<SOME TOO LONG DATA...>") + "\',\n" +
            //", itunesCategories='" + String.join(", ", itunesCategories) + '\'' +
            "\twebsiteData='" + (websiteData==null ? "null" : "<html>SOME TOO LONG DATA...</html>") + "\',\n" +
            '}';
    }
}
