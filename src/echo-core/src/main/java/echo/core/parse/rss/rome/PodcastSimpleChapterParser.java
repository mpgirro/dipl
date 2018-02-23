package echo.core.parse.rss.rome;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleParser;
import echo.core.domain.feed.Chapter;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Maximilian Irro
 */
public class PodcastSimpleChapterParser implements ModuleParser {

    private static final Namespace PSC_NS = Namespace.getNamespace(PodloveSimpleChapterModule.URI);

    @Override
    public String getNamespaceUri() {
        return PodloveSimpleChapterModule.URI;
    }

    @Override
    public Module parse(Element element, Locale locale) {
        final PodloveSimpleChapterModuleImpl mod = new PodloveSimpleChapterModuleImpl();
        if (element.getName().equals("chapters")) {
            final List<Element> chapters = element.getChildren("chapter", PSC_NS);
            final List<SimpleChapter> result = new LinkedList<>();
            for (Element chapter : chapters) {
                SimpleChapter sc = parseChapter(chapter);
                result.add(sc);
            }
            mod.setChapters(result);
            return mod;
        }
        return null;
    }

    private SimpleChapter parseChapter(Element eChapter) {
        final SimpleChapter chapter = new SimpleChapter();

        final String start = getAttributeValue(eChapter, "start");
        if (start != null) {
            chapter.setStart(start);
        }

        final String title = getAttributeValue(eChapter, "title");
        if (title != null) {
            chapter.setTitle(title);
        }

        return chapter;
    }

    protected String getAttributeValue(Element e, String attributeName) {
        Attribute attr = e.getAttribute(attributeName);
        if (attr == null) {
            attr = e.getAttribute(attributeName, PSC_NS);
        }
        if (attr != null) {
            return attr.getValue();
        } else {
            return null;
        }
    }

}
