package echo.microservice.parser.web.rest;

import echo.core.async.parser.NewFeedParserJob;
import echo.core.async.parser.UpdateFeedParserJob;
import echo.core.async.parser.WebsiteParserJob;
import echo.core.benchmark.mps.MessagesPerSecondMeter;
import echo.core.benchmark.rtt.RoundTripTime;
import echo.microservice.parser.service.ParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/parser")
public class ParserResource {

    private final Logger log = LoggerFactory.getLogger(ParserResource.class);

    @Autowired
    private ParserService parserService;

    @Resource(name = "messagesPerSecondMeter")
    private MessagesPerSecondMeter mpsMeter;

    @RequestMapping(
        value  = "/new-podcast",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void parseNewPodcastData(@RequestBody NewFeedParserJob job) {
        log.debug("REST request to parseFeed feed-data for new podcast(EXO)/feed : ({},'{}')", job.getExo(), job.getUrl());
        mpsMeter.tick();
        parserService.parseFeed(job.getExo(), job.getUrl(), job.getData(), true, RoundTripTime.empty());
    }

    @RequestMapping(
        value  = "/update-episodes",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void parseUpdateEpisodeData(@RequestBody UpdateFeedParserJob job) {
        log.debug("REST request to parseFeed feed-data to update episodes for podcast(EXO)/feed : ({},'{}')", job.getExo(), job.getUrl());
        mpsMeter.tick();
        parserService.parseFeed(job.getExo(), job.getUrl(), job.getData(), false, RoundTripTime.empty());
    }

    @RequestMapping(
        value  = "/parse-website",
        method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void parseWebsiteData(@RequestBody WebsiteParserJob job) {
        log.debug("REST request to parseFeed website-data for EXO : {}", job.getExo());
        mpsMeter.tick();
        parserService.parseWebsite(job.getExo(), job.getHtml());
    }

}
