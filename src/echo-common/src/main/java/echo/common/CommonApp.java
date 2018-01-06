package echo.common;

import com.icosillion.podengine.models.*;

import com.icosillion.podengine.exceptions.InvalidFeedException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import echo.common.dto.document.Document;
import echo.common.dto.document.EpisodeDocument;
import echo.common.dto.document.PodcastDocument;
import echo.common.index.IndexCommitter;
import echo.common.index.LuceneCommitter;
import echo.common.search.IndexSearcher;
import echo.common.search.LuceneSearcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.System.out;

/**
 * @author Maximilian Irro
 */
public class CommonApp {

    private static final String INDEX_PATH = "./index";
    private static final boolean CREATE_INDEX = true; // will re-create index on every start (for testing)

    private IndexCommitter committer;
    private IndexSearcher searcher;

    private boolean shutdown = false;
    private Map<String,String> usageMap = new HashMap();

    public static void main(String[] args) throws IOException, InvalidFeedException, MalformedFeedException {
        final CommonApp app = new CommonApp();
        app.repl();
    }

    public CommonApp() throws IOException {

        this.committer = new LuceneCommitter(INDEX_PATH, CREATE_INDEX); // TODO
        this.searcher = new LuceneSearcher(INDEX_PATH);

        // save the usages, for easy recall
        usageMap.put("index",         "feed [feed [feed]]");
        usageMap.put("search",        "query [query [query]]");
        usageMap.put("help",          "");
        usageMap.put("quit, q, exit", "");
    }

    private void repl() throws IOException, InvalidFeedException, MalformedFeedException {
        out.println();
        out.println();
        out.println("-------------------------------------------------------------------------------");
        out.println("> Welcome to Echo:Common interactive Test-App");

        while (!shutdown) {
            out.print("> ");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            final String line = reader.readLine().trim();
            final String[] commands = line.split(" ");
            final String cmd = commands[0].toLowerCase(); // 'cause we are lazy

            if (isCmd(cmd,"")) {
                // only Enter/Carriage Return was pressed, simply continue the REPL
                continue;
            } else if (isCmd(cmd,"quit","q","exit")) {
                shutdown = true;
            } else if (isCmd(cmd,"help")) {
                help();
            } else if (isCmd(cmd,"hi","hey")) {
                out.println("Hey!");
            } else if (isCmd(cmd,"index")){
                if (commands.length > 1) {
                    index(Arrays.copyOfRange(commands, 1, commands.length));
                } else {
                    usage(cmd);
                }
            } else if (isCmd(cmd,"search")){
                if (commands.length > 1) {
                    search(Arrays.copyOfRange(commands, 1, commands.length));
                } else {
                    usage(cmd);
                }
            } else {
                out.println("Unknown command '"+cmd+"'. Type 'help' for all commands");
            }
        }

        out.println("Bye!");
    }

    private boolean isCmd(String input, String cmd){
        return input.equals(cmd);
    }

    private boolean isCmd(String input, String... cmds){
        for(String cmd : cmds){
            if(isCmd(input,cmd)){
                return true;
            }
        }
        return false;
    }

    private void usage(String cmd){
        if(usageMap.containsKey(cmd)){
            final String args = usageMap.get(cmd);
            out.println("Command parsing error");
            out.println("Usage: "+cmd+" "+args);
        } else {
            out.println("No usage for '"+cmd+"' found");
        }
    }

    private void help(){
        out.println("This is an interactive REPL providing direct access to following search engine functions:");
        out.println();
        for( String key : usageMap.keySet().stream().sorted().collect(Collectors.toList())){
            out.println(key+" "+usageMap.get(key));
        }
        out.println();
        out.println("Feel free to play around!");
        out.println();
    }

    private void index(String[] feeds) throws MalformedURLException, MalformedFeedException, InvalidFeedException {

        /* remember, some useful feed urls are:
         * - https://feeds.metaebene.me/freakshow/m4a
         * - http://www.fanboys.fm/episodes.mp3.rss
         * - http://falter-radio.libsyn.com/rss
         * - http://revolutionspodcast.libsyn.com/rss/
         */

        for(String feed : feeds){
            out.println("Processing feed: " + feed);

            //Download and parse the Cortex RSS feed
            final Podcast podcast = new Podcast(new URL(feed));
            out.println("Podcast: " + podcast.getTitle() + " <" + podcast.getLink().toExternalForm() + ">");

            final PodcastDocument podcastDoc = new PodcastDocument();
            podcastDoc.setTitle(podcast.getTitle());
            podcastDoc.setLink(podcast.getLink().toExternalForm());
            podcastDoc.setDescription(podcast.getDescription());
            //podcastDoc.setLastBuildDate(podcast.getLastBuildDate()); // TODO
            podcastDoc.setLanguage(podcast.getLanguage());
            podcastDoc.setGenerator(podcast.getGenerator());

            committer.addDocument(podcastDoc);

            //Display Feed Details
            //System.out.printf("💼 %s has %d episodes!\n", podcast.getTitle(), podcast.getEpisodes().size());

            //List all episodes
            for (Episode episode : podcast.getEpisodes()) {
                //System.out.println("- " + episode.getTitle());
                out.println("  Episode: " + episode.getTitle());

                final EpisodeDocument episodeDoc = new EpisodeDocument();
                episodeDoc.setTitle(episode.getTitle());
                episodeDoc.setLink(episode.getLink().toExternalForm());
                //episodeDoc.setPubDate(episode.getPubDate()); // TODO
                episodeDoc.setGuid(episode.getGUID());
                episodeDoc.setDescription(episode.getDescription());

                this.committer.addDocument(episodeDoc);
            }
        }

        this.committer.commit();

        out.println("all done");
    }

    private void search(String[] querys){
        final String query = String.join(" ", querys);
        final Document[] results = this.searcher.search(query);
        out.println("Found "+results.length+" results for query '" + query + "'");
        out.println("Results:");
        for(Document doc : results){
            out.println();
            if(doc instanceof PodcastDocument){
                final PodcastDocument pDoc = (PodcastDocument) doc;
                out.println(pDoc.getTitle());
                out.println(pDoc.getLanguage());
                out.println(pDoc.getDescription());
                out.println(pDoc.getLink());
            } else if( doc instanceof EpisodeDocument){
                final EpisodeDocument eDoc = (EpisodeDocument) doc;
                out.println(eDoc.getTitle());
                if(eDoc.getPubDate() != null){
                    out.println(eDoc.getPubDate().toString());
                }
                out.println(eDoc.getDescription());
                out.println(eDoc.getLink());
            } else {
                throw new RuntimeException("Forgot to support new Echo Document type: "+doc.getClass());
            }
        }
    }

}