package echo.microservice.parser.web.rest;

import echo.microservice.parser.service.ParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/parser")
public class ParserResource {

    private final Logger log = LoggerFactory.getLogger(ParserResource.class);

    @Autowired
    private ParserService parserService;

    @RequestMapping(value = "/new-podcast/{podcastExo}",
        method = RequestMethod.POST,
        params = { "podcastExo", "feedUrl" })
    @ResponseStatus(HttpStatus.OK)
    public void parseNewPodcastData(@PathVariable("podcastExo") String podcastExo,
                                    @RequestParam("feedUrl") String feedUrl,
                                    @RequestBody String feedData) {
        log.debug("REST request to parseFeed feed-data for new podcast(EXO)/feed : ({},'{}')", podcastExo, feedUrl);
        parserService.parseFeed(podcastExo, feedUrl, feedData, true);
    }

    @RequestMapping(value = "/update-episodes/{podcastExo}",
        method = RequestMethod.POST,
        params = { "podcastExo", "feedUrl" })
    @ResponseStatus(HttpStatus.OK)
    public void parseUpdateEpisodeData(@PathVariable("podcastExo") String podcastExo,
                                       @RequestParam("feedUrl") String feedUrl,
                                       @RequestBody String feedData) {
        log.debug("REST request to parseFeed feed-data to update episodes for podcast(EXO)/feed : ({},'{}')", podcastExo, feedUrl);
        parserService.parseFeed(podcastExo, feedUrl, feedData, false);
    }

    @RequestMapping(value = "/parse-website/{exo}",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void parseWebsiteData(@PathVariable("exo") String exo,
                                 @RequestBody String html) {
        log.debug("REST request to parseFeed website-data for EXO : {}", exo);
        parserService.parseWebsite(exo, html);
    }

}
