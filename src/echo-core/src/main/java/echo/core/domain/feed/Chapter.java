package echo.core.domain.feed;

/**
 * @author Maximilian Irro
 */
public class Chapter {

    private String start;
    private String title;
    private String href;
    private String image;

    public Chapter() { }

    public Chapter(Chapter c) {
        this.start = c.getStart();
        this.title = c.getTitle();
        this.href = c.getHref();
        this.image = c.getImage();
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

    @Override
    public String toString() {
        return "Chapter{" +
            "start='" + start + '\'' +
            ", title='" + title + '\'' +
            ", href='" + href + '\'' +
            ", image='" + image + '\'' +
            '}';
    }
}
