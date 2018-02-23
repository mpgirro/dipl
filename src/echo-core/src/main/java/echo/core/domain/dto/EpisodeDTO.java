package echo.core.domain.dto;

import echo.core.domain.feed.ChapterDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
public class EpisodeDTO implements EntityDTO {

    private Long id;
    private String echoId;
    private Long podcastId;

    private String title;
    private String link;
    private LocalDateTime pubDate;
    private String guid;
    private Boolean guidIsPermaLink;
    private String description;

    private String itunesImage;
    private String itunesDuration;
    private String itunesSubtitle;
    private String itunesAuthor;
    private String itunesSummary;
    private Integer itunesSeason;
    private Integer itunesEpisode;
    private String itunesEpisodeType;

    private String enclosureUrl;
    private Long enclosureLength;
    private String enclosureType;

    private String contentEncoded;
    private String websiteData;

    private LocalDateTime registrationTimestamp;

    private List<ChapterDTO> chapters;

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

    public Long getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(Long podcastId) {
        this.podcastId = podcastId;
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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Boolean isGuidIsPermaLink() {
        return guidIsPermaLink;
    }

    public void setGuidIsPermaLink(Boolean guidIsPermaLink) {
        this.guidIsPermaLink = guidIsPermaLink;
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

    public String getItunesSubtitle() {
        return itunesSubtitle;
    }

    public void setItunesSubtitle(String itunesSubtitle) {
        this.itunesSubtitle = itunesSubtitle;
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

    public Integer getItunesSeason() {
        return itunesSeason;
    }

    public void setItunesSeason(Integer itunesSeason) {
        this.itunesSeason = itunesSeason;
    }

    public Integer getItunesEpisode() {
        return itunesEpisode;
    }

    public void setItunesEpisode(Integer itunesEpisode) {
        this.itunesEpisode = itunesEpisode;
    }

    public String getItunesEpisodeType() {
        return itunesEpisodeType;
    }

    public void setItunesEpisodeType(String itunesEpisodeType) {
        this.itunesEpisodeType = itunesEpisodeType;
    }

    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    public Long getEnclosureLength() {
        return enclosureLength;
    }

    public void setEnclosureLength(Long enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    public String getEnclosureType() {
        return enclosureType;
    }

    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    public String getContentEncoded() {
        return contentEncoded;
    }

    public void setContentEncoded(String contentEncoded) {
        this.contentEncoded = contentEncoded;
    }

    public String getWebsiteData() {
        return websiteData;
    }

    public void setWebsiteData(String websiteData) {
        this.websiteData = websiteData;
    }

    public LocalDateTime getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(LocalDateTime registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public List<ChapterDTO> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDTO> chapters) {
        this.chapters = chapters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpisodeDTO that = (EpisodeDTO) o;

        return echoId.equals(that.echoId);
    }

    @Override
    public int hashCode() {
        return echoId.hashCode();
    }

    @Override
    public String toString() {
        return "EpisodeDTO{\n" +
            "\tid=" + id + ",\n" +
            "\techoId='" + echoId + "\',\n" +
            "\tpodcastId=" + podcastId + "\',\n" +
            "\ttitle='" + title + "\',\n" +
            "\tlink='" + link + "\',\n" +
            "\tpubDate=" + pubDate + ",\n" +
            "\tguid='" + guid + "\',\n" +
            "\tguidIsPermaLink=" + guidIsPermaLink + ",\n" +
            "\titunesImage='" + itunesImage + "\',\n" +
            "\titunesDuration='" + itunesDuration + "\',\n" +
            "\titunesSubtitle='" + itunesSubtitle + "\',\n" +
            "\titunesAuthor='" + itunesAuthor + "\',\n" +
            "\titunesSummary='" + itunesSummary + "\',\n" +
            "\titunesSeason='" + itunesSeason + "\',\n" +
            "\titunesEpisode='" + itunesEpisode + "\',\n" +
            "\titunesEpisodeType='" + itunesEpisodeType + "\',\n" +
            "\tenclosureUrl='" + enclosureUrl + "\',\n" +
            "\tenclosureLength='" + enclosureLength + "\',\n" +
            "\tenclosureType='" + enclosureType + "\',\n" +
            "\tregistrationTimestamp=" + registrationTimestamp + "\',\n" +
            "\tchapters='" + ((chapters!=null) ? String.join("\n", chapters.stream().map(ChapterDTO::toString).collect(Collectors.toList())) : null) + "\',\n" +
            "\tdescription='" + description + "\',\n" +
            "\tcontentEncoded='" + contentEncoded + "\',\n" +
            "\twebsiteData='" + (websiteData==null ? "null" : "<html>SOME TOO LONG DATA...</html>") + "\',\n" +
            '}';
    }
}
