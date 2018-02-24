package echo.core.util;

import echo.core.domain.dto.EntityDTO;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.PodcastDTO;
import org.jsoup.*;

/**
 * @author Maximilian Irro
 */
public class DocumentFormatter {

    private static final String NEWLINE = System.getProperty("line.separator");

    public static String cliFormat(EntityDTO dto){
        return dto.toString();
    }

    public static String cliFormat(IndexDocDTO doc){
        final StringBuilder builder = new StringBuilder();
        switch (doc.getDocType()) {
            case "podcast":
                builder
                    .append(doc.getTitle())
                    .append(NEWLINE)
                    .append("[Podcast] ")
                    .append(NEWLINE);
                if (doc.getPubDate() != null) builder.append(doc.getPubDate());
                builder
                    .append(NEWLINE)
                    .append(Jsoup.parse(doc.getDescription()).text())
                    .append(NEWLINE)
                    .append(doc.getLink())
                    .append(NEWLINE);
                break;
            case "episode":
                builder
                    .append(doc.getTitle())
                    .append(NEWLINE)
                    .append(doc.getPodcastTitle())
                    .append(NEWLINE)
                    .append("[Episode] ");
                if (doc.getPubDate() != null) builder.append(doc.getPubDate());
                builder
                    .append(NEWLINE)
                    .append(Jsoup.parse(doc.getDescription()).text())
                    .append(NEWLINE)
                    .append(doc.getLink())
                    .append(NEWLINE);
                break;
            default:
                throw new RuntimeException("Forgot to support new Echo EntityDTO type: " + doc.getClass());
        }
        return builder.toString();
    }

}
