package echo.core;

import com.icosillion.podengine.models.*;

import com.icosillion.podengine.exceptions.InvalidFeedException;
import com.icosillion.podengine.exceptions.MalformedFeedException;
import echo.core.dto.document.Document;
import echo.core.dto.document.EpisodeDocument;
import echo.core.dto.document.PodcastDocument;
import echo.core.index.IndexCommitter;
import echo.core.index.LuceneCommitter;
import echo.core.search.IndexSearcher;
import echo.core.search.LuceneSearcher;

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
public class CoreApp {

    private static final String INDEX_PATH = "./index";
    private static final boolean CREATE_INDEX = true; // will re-create index on every start (for testing)

    private IndexCommitter committer;
    private IndexSearcher searcher;

    private boolean shutdown = false;
    private Map<String,String> usageMap = new HashMap();

    public static void main(String[] args) throws IOException, InvalidFeedException, MalformedFeedException {
        final CoreApp app = new CoreApp();
        app.repl();
    }

    public CoreApp() throws IOException {

        this.committer = new LuceneCommitter(INDEX_PATH, CREATE_INDEX); // TODO
        this.searcher = new LuceneSearcher(((LuceneCommitter)this.committer).getIndexWriter());

        // auto-destroy on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            searcher.destroy();
            committer.destroy();
        }));

        // save the usages, for easy recall
        usageMap.put("index",             "feed [feed [feed]]");
        usageMap.put("search",            "query [query [query]]");
        usageMap.put("test-index",        "");
        usageMap.put("test-index-search", "");
        usageMap.put("help",              "");
        usageMap.put("quit, q, exit",     "");
    }

    private void repl() throws IOException, InvalidFeedException, MalformedFeedException {
        out.println("> Welcome to Echo:Core interactive exploration App!");

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
            } else if (isCmd(cmd,"search")) {
                if (commands.length > 1) {
                    search(Arrays.copyOfRange(commands, 1, commands.length));
                } else {
                    usage(cmd);
                }
            } else if(isCmd(cmd,"test-index")){
                index(new String[]{
                    "https://feeds.metaebene.me/freakshow/m4a",
                    "http://www.fanboys.fm/episodes.mp3.rss",
                    "http://falter-radio.libsyn.com/rss",
                    "http://revolutionspodcast.libsyn.com/rss/",
                    "https://feeds.metaebene.me/forschergeist/m4a",
                    "http://feeds.soundcloud.com/users/soundcloud:users:325487962/sounds.rss", // Ganz offen gesagt feed
                });

            } else if(isCmd(cmd,"test-index-search")){
                index(new String[]{"https://feeds.metaebene.me/freakshow/m4a"});
                searcher.refresh(); // I need to manually refresh here, otherwise there will be no results because auto-refresh has not triggered yet
                search(new String[]{"Sendung"});
            } else {
                out.println("Unknown command '"+cmd+"'. Type 'help' for all commands");
            }
        }

        out.println("Bye!\n");
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

            committer.add(podcastDoc);

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

                this.committer.add(episodeDoc);
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
                out.println("[Podcast]");
                out.println(pDoc.getTitle());
                out.println(pDoc.getLanguage());
                out.println(pDoc.getDescription());
                out.println(pDoc.getLink());
            } else if( doc instanceof EpisodeDocument){
                final EpisodeDocument eDoc = (EpisodeDocument) doc;
                out.println("[Episode]");
                out.println(eDoc.getTitle());
                if(eDoc.getPubDate() != null){
                    out.println(eDoc.getPubDate().toString());
                }
                out.println(eDoc.getDescription());
                out.println(eDoc.getLink());
            } else {
                throw new RuntimeException("Forgot to support new Echo Document type: "+doc.getClass());
            }
            out.println("\n---");
        }
        out.println();
    }

}
