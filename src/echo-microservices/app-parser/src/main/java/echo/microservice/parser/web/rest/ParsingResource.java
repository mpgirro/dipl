package echo.microservice.parser.web.rest;

import echo.microservice.parser.service.ParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/api/parser")
public class ParsingResource {

    private final Logger log = LoggerFactory.getLogger(ParsingResource.class);

    @Autowired
    private ParsingService parsingService;

    @RequestMapping(value = "/new-podcast/{podcastExo}",
        method = RequestMethod.POST,
        params = { "podcastExo", "feedUrl" })
    public void parseNewPodcastData(@PathVariable("podcastExo") String podcastExo,
                                    @RequestParam("feedUrl") String feedUrl,
                                    @RequestBody String feedData) {
        log.debug("REST request to parseFeed feed-data for new podcast(EXO)/feed : ({},'{}')", podcastExo, feedUrl);
        parsingService.parseFeed(podcastExo, feedUrl, feedData, true);
    }

    @RequestMapping(value = "/update-episodes/{podcastExo}",
        method = RequestMethod.POST,
        params = { "podcastExo", "feedUrl" })
    public void parseUpdateEpisodeData(@PathVariable("podcastExo") String podcastExo,
                                       @RequestParam("feedUrl") String feedUrl,
                                       @RequestBody String feedData) {
        log.debug("REST request to parseFeed feed-data to update episodes for podcast(EXO)/feed : ({},'{}')", podcastExo, feedUrl);
        parsingService.parseFeed(podcastExo, feedUrl, feedData, false);
    }

    @RequestMapping(value = "/parse-website/{exo}",
        method = RequestMethod.POST)
    public void parseWebsiteData(@PathVariable("exo") String exo,
                                 @RequestBody String html) {
        log.debug("REST request to parseFeed website-data for EXO : {}", exo);
        parsingService.parseWebsite(exo, html);
    }

}
