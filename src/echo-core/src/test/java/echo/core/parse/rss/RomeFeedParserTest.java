package echo.core.parse.rss;

import echo.core.domain.dto.PodcastDTO;
import echo.core.exception.FeedParsingException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maximilian Irro
 */
public class RomeFeedParserTest {

    private static final Logger log = LoggerFactory.getLogger(RomeFeedParserTest.class);

    private static String feedData;

    @BeforeAll
    static void setup() throws IOException {
        feedData = Files.lines(Paths.get("src","test","resources","testfeed.xml")).collect(Collectors.joining("\n"));
    }

    @AfterAll
    static void done() {

    }

    @BeforeEach
    void init() {

    }

    @AfterEach
    void tearDown() {

    }

    @DisplayName("All Podcast fields are not null or empty")
    @Test
    void test_podcastFieldAreParsed() throws FeedParsingException {
        final FeedParser parser = RomeFeedParser.of(feedData);
        final PodcastDTO p = parser.getPodcast();

        assertFalse(isNullOrEmpty(p.getTitle()), "<title> is null or empty");
        assertFalse(isNullOrEmpty(p.getLink()), "<link> is null or empty");
        assertFalse(isNullOrEmpty(p.getDescription()), "<description> is null or empty");
        assertFalse(isNullOrEmpty(p.getCopyright()), "<copyright> is null or empty");
        assertFalse(isNullOrEmpty(p.getDocs()), "<doc> is null or empty");
        assertFalse(isNullOrEmpty(p.getGenerator()), "<generator> is null or empty");
        assertFalse(isNullOrEmpty(p.getImage()), "<image> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesAuthor()), "<itunes:author> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesKeywords()), "<itunes:keywords> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesOwnerEmail()), "<itunes:owner><itunes:email> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesOwnerName()), "<itunes:owner><itunes:name> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesSummary()), "<itunes:summary> is null or empty");
        assertFalse(isNullOrEmpty(p.getItunesType()), "<itunes:type> is null or empty");
        assertNotNull(p.getItunesExplicit(), "<itunes:explicit> is null or empty");
        assertNotNull(p.getItunesBlock(), "<itunes:block> is null or empty");
        assertNotNull(p.getItunesCategories(), "<itunes:category>> is null");
        assertNotEquals(0, p.getItunesCategories().size());
        p.getItunesCategories().stream()
            .forEach(c -> assertFalse(isNullOrEmpty(c), "<itunes:category> is null or empty"));
    }

}
