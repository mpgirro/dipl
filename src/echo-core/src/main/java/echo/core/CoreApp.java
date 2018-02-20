package echo.core;

import echo.core.exception.FeedParsingException;
import echo.core.exception.SearchException;
import echo.core.index.IndexCommitter;
import echo.core.index.LuceneCommitter;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.IndexDocDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.dto.ResultWrapperDTO;
import echo.core.parse.api.API;
import echo.core.parse.api.FyydAPI;
import echo.core.parse.rss.FeedParser;
import echo.core.parse.rss.PodEngineFeedParser;
import echo.core.index.IndexSearcher;
import echo.core.index.LuceneSearcher;
import echo.core.util.DocumentFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.out;

/**
 * @author Maximilian Irro
 */
public class CoreApp {

    private static final String INDEX_PATH = "./index";
    private static final boolean CREATE_INDEX = true; // will re-create index on every start (for testing)

    private FeedParser feedParser;
    private IndexCommitter committer;
    private IndexSearcher searcher;

    private boolean shutdown = false;
    private Map<String,String> usageMap = new HashMap();

    private final API fyydAPI = new FyydAPI();

    public static void main(String[] args) throws Exception {
        final CoreApp app = new CoreApp();
        app.repl();
    }

    public CoreApp() throws IOException {

        this.feedParser = new PodEngineFeedParser();
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
        usageMap.put("print-fyyd-feeds",  "count");
    }

    private void repl() throws Exception {
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

                final String fileName = "../feeds.txt";

                //read file into stream, try-with-resources
                try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
                    stream.forEach(this::index);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if(isCmd(cmd,"test-index-search")){
                index(new String[]{"https://feeds.metaebene.me/freakshow/m4a"});
                searcher.refresh(); // I need to manually refresh here, otherwise there will be no results because auto-refresh has not triggered yet
                search(new String[]{"Sendung"});
            } else if (isCmd(cmd,"print-fyyd-feeds")) {
                if (commands.length == 2) {
                    final int count = Integer.valueOf(commands[1]);
                    out.println("These are "+count+" feed URLs from fyyd.de");
                    for(String feed : fyydAPI.getFeedUrls(count)){
                        out.println("\t"+feed);
                    }
                } else {
                    usage(cmd);
                }
            } else {
                out.println("Unknown command '"+cmd+"'. Type 'help' for all commands");
            }
        }

        out.println("Bye!\n");
        System.exit(0);
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

    private void index(String feed) {
        out.println("Processing feed: " + feed);

        try {
            final String feedData = download(feed);

            final PodcastDTO podcastDoc = this.feedParser.parseFeed(feedData);
            podcastDoc.setEchoId(feed);

            this.committer.add(podcastDoc);

            final List<EpisodeDTO> episodes = ((PodEngineFeedParser) feedParser).extractEpisodes(feedData);
            for (EpisodeDTO episode : episodes) {
                out.println("  Episode: " + episode.getTitle());

                episode.setEchoId(episode.getGuid()); // TODO verifiy good GUID!

                this.committer.add(episode);
            }
        } catch (IOException | FeedParsingException e) {
            e.printStackTrace();
        }
        this.committer.commit();
    }

    private void index(String[] feeds) {

        for(String feed : feeds){
            index(feed);
        }
        out.println("all done");
    }

    private void search(String[] querys) throws SearchException {

        searcher.refresh(); // ensure there is data accessible to us in the index

        final String query = String.join(" ", querys);
        final ResultWrapperDTO results = this.searcher.search(query, 1, 100);
        out.println("Found "+results.getTotalHits()+" results for query '" + query + "'");
        out.println("Results:");
        for(IndexDocDTO doc : results.getResults()){
            out.println();
            out.println(DocumentFormatter.cliFormat(doc));
            out.println();
        }
    }

    private String download(String feedUrl) throws IOException {

        return new Scanner(new URL(feedUrl).openStream(), "UTF-8")
            .useDelimiter("\\A")
            .next();

    }

}
