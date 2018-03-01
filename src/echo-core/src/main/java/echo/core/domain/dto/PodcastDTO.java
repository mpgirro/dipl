package echo.core.domain.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Maximilian Irro
 */
public class PodcastDTO implements EntityDTO {

    private Long id;
    private String echoId;

    private String title;
    private String link;
    private String description;
    private LocalDateTime pubDate;
    private LocalDateTime lastBuildDate;
    private String language;
    private String generator;
    private String copyright;
    private String docs;
    private String managingEditor;
    private String image;

    private String itunesSummary;
    private String itunesAuthor;
    private String itunesKeywords;
    private Set<String> itunesCategories;
    private Boolean itunesExplicit;
    private Boolean itunesBlock;
    private String itunesType;
    private String itunesOwnerName;
    private String itunesOwnerEmail;

    private String feedpressLocale;
    private String fyydVerify;

    private int episodeCount;

    private LocalDateTime registrationTimestamp;
    private Boolean registrationComplete;

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

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getDocs() {
        return docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public String getManagingEditor() {
        return managingEditor;
    }

    public void setManagingEditor(String managingEditor) {
        this.managingEditor = managingEditor;
    }

    public String getItunesSummary() {
        return itunesSummary;
    }

    public void setItunesSummary(String itunesSummary) {
        this.itunesSummary = itunesSummary;
    }

    public String getItunesAuthor() {
        return itunesAuthor;
    }

    public void setItunesAuthor(String itunesAuthor) {
        this.itunesAuthor = itunesAuthor;
    }

    public String getItunesKeywords() {
        return itunesKeywords;
    }

    public void setItunesKeywords(String itunesKeywords) {
        this.itunesKeywords = itunesKeywords;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<String> getItunesCategories() {
        return itunesCategories;
    }

    public void setItunesCategories(Set<String> itunesCategories) {
        this.itunesCategories = itunesCategories;
    }

    public Boolean getItunesExplicit() {
        return itunesExplicit;
    }

    public void setItunesExplicit(Boolean itunesExplicit) {
        this.itunesExplicit = itunesExplicit;
    }

    public Boolean getItunesBlock() {
        return itunesBlock;
    }

    public void setItunesBlock(Boolean itunesBlock) {
        this.itunesBlock = itunesBlock;
    }

    public String getItunesType() {
        return itunesType;
    }

    public void setItunesType(String itunesType) {
        this.itunesType = itunesType;
    }

    public String getItunesOwnerName() {
        return itunesOwnerName;
    }

    public void setItunesOwnerName(String itunesOwnerName) {
        this.itunesOwnerName = itunesOwnerName;
    }

    public String getItunesOwnerEmail() {
        return itunesOwnerEmail;
    }

    public void setItunesOwnerEmail(String itunesOwnerEmail) {
        this.itunesOwnerEmail = itunesOwnerEmail;
    }

    public String getFeedpressLocale() {
        return feedpressLocale;
    }

    public void setFeedpressLocale(String feedpressLocale) {
        this.feedpressLocale = feedpressLocale;
    }

    public String getFyydVerify() {
        return fyydVerify;
    }

    public void setFyydVerify(String fyydVerify) {
        this.fyydVerify = fyydVerify;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public LocalDateTime getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(LocalDateTime registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public Boolean getRegistrationComplete() {
        return registrationComplete;
    }

    public void setRegistrationComplete(Boolean registrationComplete) {
        this.registrationComplete = registrationComplete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PodcastDTO that = (PodcastDTO) o;

        return echoId.equals(that.echoId);
    }

    @Override
    public int hashCode() {
        return echoId.hashCode();
    }

    @Override
    public String toString() {
        return "PodcastDTO{\n" +
            "\tid=" + id + ",\n" +
            "\techoId='" + echoId + "\',\n" +
            "\ttitle='" + title + "\',\n" +
            "\tlink='" + link + "\',\n" +
            "\tpubDate=" + pubDate + ",\n" +
            "\tlastBuildDate=" + lastBuildDate + ",\n" +
            "\tlanguage='" + language + "\',\n" +
            "\tgenerator='" + generator + "\',\n" +
            "\tcopyright='" + copyright + "\',\n" +
            "\tdocs='" + docs + "\',\n" +
            "\tmanagingEditor='" + managingEditor + "\',\n" +
            "\titunesCategories='" + String.join(", ", itunesCategories) + "\',\n" +
            "\titunesSummary='" + itunesSummary + "\',\n" +
            "\titunesAuthor='" + itunesAuthor + "\',\n" +
            "\titunesKeywords='" + itunesKeywords + "\',\n" +
            "\timage='" + image + "\',\n" +
            "\titunesCategories=" + itunesCategories + ",\n" +
            "\titunesExplicit=" + itunesExplicit + ",\n" +
            "\titunesBlock=" + itunesBlock + ",\n" +
            "\titunesType='" + itunesType + "\',\n" +
            "\titunesOwnerName='" + itunesOwnerName + "\',\n" +
            "\titunesOwnerEmail='" + itunesOwnerEmail + "\',\n" +
            "\tfeedpressLocale='" + feedpressLocale + "\',\n" +
            "\tfyydVerify='" + fyydVerify + "\',\n" +
            "\tepisodeCount=" + episodeCount + ",\n" +
            "\tregistrationTimestamp=" + registrationTimestamp + ",\n" +
            "\tregistrationComplete=" + registrationComplete + ",\n" +
            "\tdescription='" + description + "\',\n" +
            '}';
    }
}
