package echo.core.domain.dto;

/**
 * @author Maximilian Irro
 */
public class ChapterDTO {

    private Long id;
    private String start;
    private String title;
    private String href;
    private String image;
    private Long episodeId;
    private String episodeExo;

    public ChapterDTO() { }

    public ChapterDTO(ChapterDTO c) {
        this.start = c.getStart();
        this.title = c.getTitle();
        this.href = c.getHref();
        this.image = c.getImage();
        this.episodeExo = c.getEpisodeExo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(Long episodeId) {
        this.episodeId = episodeId;
    }

    public String getEpisodeExo() {
        return episodeExo;
    }

    public void setEpisodeExo(String episodeExo) {
        this.episodeExo = episodeExo;
    }

    @Override
    public String toString() {
        return "ChapterDTO{" +
            "id=" + id +
            ", start='" + start + '\'' +
            ", title='" + title + '\'' +
            ", href='" + href + '\'' +
            ", image='" + image + '\'' +
            ", episodeId=" + episodeId +
            ", episodeExo=" + episodeExo +
            '}';
    }
}
