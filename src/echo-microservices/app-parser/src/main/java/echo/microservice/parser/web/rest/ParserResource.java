package echo.microservice.parser.web.rest;

import echo.core.async.job.ParserJob;
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

    @RequestMapping(
        value  = "/new-podcast",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void parseNewPodcastData(@RequestBody ParserJob job) {
        log.debug("REST request to parseFeed feed-data for new podcast(EXO)/feed : ({},'{}')", job.getExo(), job.getUrl());
        parserService.parseFeed(job, true);
    }

    @RequestMapping(
        value  = "/update-episodes",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void parseUpdateEpisodeData(@RequestBody ParserJob job) {
        log.debug("REST request to parseFeed feed-data to update episodes for podcast(EXO)/feed : ({},'{}')", job.getExo(), job.getUrl());
        parserService.parseFeed(job, false);
    }

    @RequestMapping(
        value  = "/parse-website",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void parseWebsiteData(@RequestBody ParserJob job) {
        log.debug("REST request to parseFeed website-data for EXO : {}", job.getExo());
        parserService.parseWebsite(job);
    }

}
