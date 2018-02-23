package echo.core.parse.rss.rome;

import com.rometools.rome.feed.CopyFrom;

/**
 * @author Maximilian Irro
 */
public class SimpleChapter implements CopyFrom {

    private String start;
    private String title;

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

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return SimpleChapter.class;
    }

    @Override
    public void copyFrom(CopyFrom obj) {
        SimpleChapter sc = (SimpleChapter) obj;
        setStart(sc.getStart());
        setTitle(sc.getTitle());
    }

    @Override
    public String toString() {
        return "SimpleChapter{" +
            "start='" + start + '\'' +
            ", title='" + title + '\'' +
            '}';
    }
}
