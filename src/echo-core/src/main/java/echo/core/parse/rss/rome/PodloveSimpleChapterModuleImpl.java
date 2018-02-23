package echo.core.parse.rss.rome;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;
import com.rometools.rome.feed.module.ModuleImpl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Maximilian Irro
 */

public class PodloveSimpleChapterModuleImpl extends ModuleImpl implements PodloveSimpleChapterModule, Cloneable, Serializable {

    private List<SimpleChapter> chapters;


    public PodloveSimpleChapterModuleImpl() {
        super(PodloveSimpleChapterModule.class, PodloveSimpleChapterModule.URI);
    }


    @Override
    public List<SimpleChapter> getChapters() {
        return (chapters==null) ? (chapters= new LinkedList<>()) : chapters;
    }

    @Override
    public void setChapters(List<SimpleChapter> chapters) {
        this.chapters = chapters;
    }

    @Override
    public Class<? extends CopyFrom> getInterface() {
        return PodloveSimpleChapterModule.class;
    }

    @Override
    public void copyFrom(CopyFrom obj) {
        final PodloveSimpleChapterModule sm = (PodloveSimpleChapterModule) obj;
        final List<SimpleChapter> chapters = new LinkedList<>();
        for(SimpleChapter chapter : sm.getChapters()) {
            final SimpleChapter sc = new SimpleChapter();
            sc.copyFrom(chapter);
            //sc.setTitle(chapter.getTitle());
            //sc.setStart(chapter.getStart());
            chapters.add(sc);
        }
        setChapters(chapters);
    }

    @Override
    public String getUri() {
        return PodloveSimpleChapterModule.URI;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final PodloveSimpleChapterModuleImpl m = new PodloveSimpleChapterModuleImpl();
        final List<SimpleChapter> result = new LinkedList<>();
        for (SimpleChapter chapter : this.chapters){
            SimpleChapter sc = new SimpleChapter();
            sc.copyFrom(chapter);
            //sc.setTitle(chapter.getTitle());
            //sc.setStart(chapter.getStart());
            result.add(sc);
        }
        result.subList(0, result.size()); // not sure why I need to do this
        m.setChapters(result);
        return m;
    }

    @Override
    public boolean equals(final Object obj) {
        final EqualsBean eBean = new EqualsBean(PodloveSimpleChapterModuleImpl.class, this);
        return eBean.beanEquals(obj);
    }

    @Override
    public int hashCode() {
        final EqualsBean equals = new EqualsBean(PodloveSimpleChapterModuleImpl.class, this);
        return equals.beanHashCode();
    }

    @Override
    public String toString() {
        final ToStringBean tsBean = new ToStringBean(PodloveSimpleChapterModuleImpl.class, this);
        return tsBean.toString();
    }
}
