package echo.core.benchmark;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import echo.core.exception.FeedParsingException;
import echo.core.parse.rss.FeedParser;
import echo.core.parse.rss.RomeFeedParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static java.lang.System.out;

/**
 * @author Maximilian Irro
 */
public class FeedPropertyUtil {

    private static final Logger log = LoggerFactory.getLogger(FeedPropertyUtil.class);

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(FeedProperty.class, new FeedPropertyInterfaceAdapter())
        .create();

    public ImmutableList<FeedProperty> generateFeedProperties(String feedsListFile, String feedDestDir) throws IOException, FeedParsingException {
        log.debug("Generating feed properties for feeds in : {} -- saving feeds to : {}", feedsListFile, feedDestDir);

        final List<FeedProperty> properties = new LinkedList<>();
        final Stream<String> feedsStream = Files.lines(Paths.get(feedsListFile));
        for (String url : feedsStream.toArray(String[]::new)) {
            final String feedData = download(url);
            final String fileName = url.replaceAll("[\\\\/:*?\"<>|]", "_") + ".xml";

            final File file = new File(feedDestDir+fileName);
            final String path = file.getCanonicalPath();
            writeToFile(path, feedData);

            final FeedParser feedParser = RomeFeedParser.of(feedData);
            final FeedProperty property = ImmutableFeedProperty.of(url, path, feedParser.getEpisodes().size());
            properties.add(property);
        }

        return ImmutableList.copyOf(properties);
    }

    public ImmutableList<FeedProperty> loadProperties(String filePath) throws IOException {
        log.debug("Loading feed properties from file : {}", filePath);
        final List<FeedProperty> results;
        try (Reader reader = new FileReader(filePath)) {
            results = gson.fromJson(reader, new TypeToken<List<FeedProperty>>(){}.getType());
        }
        return ImmutableList.copyOf(results);
    }

    public void saveProperties(List<FeedProperty> properties, String filePath) throws IOException {
        log.debug("Saving feed properties to file : {}", filePath);
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(properties, writer);
        }
    }

    private String download(String feedUrl) throws IOException {
        log.debug("Downloading : {}", feedUrl);
        return new Scanner(new URL(feedUrl).openStream(), "UTF-8")
            .useDelimiter("\\A")
            .next();

    }

    private void writeToFile(String dest, String content) throws IOException {
        final Path path = Paths.get(dest);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(content);
        }
    }

}
