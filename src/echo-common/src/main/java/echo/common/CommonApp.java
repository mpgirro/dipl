package echo.common;

import com.icosillion.podengine.models.*;

import com.icosillion.podengine.exceptions.DateFormatException;
import com.icosillion.podengine.exceptions.InvalidFeedException;
import com.icosillion.podengine.exceptions.MalformedFeedException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.System.out;

public class CommonApp {

    public static void main(String[] args) throws IOException, InvalidFeedException, MalformedFeedException {

        //Download and parse the Cortex RSS feed
        //Podcast podcast = new Podcast(new URL("http://freakshow.fm/feed/m4a/"));
        Podcast podcast = new Podcast(new URL("https://feeds.metaebene.me/freakshow/m4a"));

        //Display Feed Details
        System.out.printf("💼 %s has %d episodes!\n", podcast.getTitle(), podcast.getEpisodes().size());

        //List all episodes
        for (Episode episode : podcast.getEpisodes()) {
            System.out.println("- " + episode.getTitle());
        }

    }

}
