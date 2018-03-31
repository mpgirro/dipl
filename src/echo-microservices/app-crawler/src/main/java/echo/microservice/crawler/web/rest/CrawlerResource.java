package echo.microservice.crawler.web.rest;

import echo.microservice.crawler.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

/**
 * @author Maximilian Irro
 */
@RestController
@RequestMapping("/crawler")
public class CrawlerResource {

    private final Logger log = LoggerFactory.getLogger(CrawlerResource.class);

    @Autowired
    private CrawlerService crawlerService;

    @RequestMapping(value = "/download-new-feed",
        method = RequestMethod.POST,
        params = { "exo", "url" })
    @ResponseStatus(HttpStatus.OK)
    public void downloadNewFeed(@RequestParam("exo") String exo,
                                @RequestParam("url") String url) throws URISyntaxException {
        log.debug("REST request to download feed by EXO/URL : ({},{})", exo, url);
        crawlerService.downloadFeed(exo, url, true);
    }

    @RequestMapping(value = "/download-update-feed",
        method = RequestMethod.POST,
        params = { "exo", "url" })
    @ResponseStatus(HttpStatus.OK)
    public void downloadUpdateFeed(@RequestParam("exo") String exo,
                                   @RequestParam("url") String url) throws URISyntaxException {
        log.debug("REST request to download feed by EXO/URL : ({},{})", exo, url);
        crawlerService.downloadFeed(exo, url, false);
    }

    @RequestMapping(value = "/download-website",
        method = RequestMethod.POST,
        params = { "exo", "url" })
    @ResponseStatus(HttpStatus.OK)
    public void downloadWebsite(@RequestParam("exo") String exo,
                                @RequestParam("url") String url) throws URISyntaxException {
        log.debug("REST request to download website by EXO/URL : ({},{})", exo, url);
        crawlerService.downloadWebsite(exo, url);
    }

}
