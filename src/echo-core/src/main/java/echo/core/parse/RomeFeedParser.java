package echo.core.parse;

import com.rometools.modules.atom.modules.AtomLinkModule;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import echo.core.dto.document.EpisodeDocument;
import echo.core.dto.document.PodcastDocument;
import echo.core.exception.FeedParsingException;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.rome.feed.atom.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
public class RomeFeedParser implements FeedParser {

    private static final Logger log = LoggerFactory.getLogger(RomeFeedParser.class);

    @Override
    public PodcastDocument parseFeed(String xmlData) throws FeedParsingException {
        try {
            final InputSource inputSource = new InputSource( new StringReader( xmlData ) );
            final SyndFeedInput input = new SyndFeedInput();
            final SyndFeed syndFeed = input.build(inputSource);

            final PodcastDocument doc = new PodcastDocument();

            doc.setTitle(syndFeed.getTitle());
            doc.setLink(syndFeed.getLink());
            doc.setDescription(syndFeed.getDescription());
            if(syndFeed.getPublishedDate() != null){
                doc.setPubDate(LocalDateTime.ofInstant(syndFeed.getPublishedDate().toInstant(), ZoneId.systemDefault()));
            }
            doc.setLanguage(syndFeed.getLanguage());
            doc.setGenerator(syndFeed.getGenerator());

            final Module itunesFeedModule = syndFeed.getModule("http://www.itunes.com/dtds/podcast-1.0.dtd");
            final FeedInformation itunesFeedInfo = (FeedInformation) itunesFeedModule;
            if(itunesFeedInfo.getImage() != null){
                doc.setItunesImage(itunesFeedInfo.getImage().toExternalForm());
            }
            doc.setItunesCategory(String.join(" | ", itunesFeedInfo.getCategories().stream().map(c->c.getName()).collect(Collectors.toCollection(LinkedList::new))));

            // here I process the feed specific atom Links
            final List<Link> atomLinks = getAtomLinks(syndFeed);
            for(Link atomLink : atomLinks){
                if(atomLink.getRel().equals("http://podlove.org/deep-link")){
                    // TODO
                } else if(atomLink.getRel().equals("payment")){
                    // TODO
                } else if(atomLink.getRel().equals("self")){
                    // TODO
                } else if(atomLink.getRel().equals("alternate")){
                    // TODO
                } else if(atomLink.getRel().equals("first")){
                    // TODO
                } else if(atomLink.getRel().equals("next")){
                    // TODO
                } else if(atomLink.getRel().equals("last")){
                    // TODO
                } else if(atomLink.getRel().equals("hub")){
                    // TODO
                } else {
                    log.warn("Came across an <atom:link> with a relation I do not handle: rel={}", atomLink.getRel());
                }
            }

            return doc;

        } catch (FeedException e) {
            throw new FeedParsingException("RomeFeedParser could not parse the feed", e);
        }
    }

    @Override
    public EpisodeDocument parseEpisode(String xmlData) throws FeedParsingException {
        throw new UnsupportedOperationException("RomeFeedParser.parseEpisode not yet implemented");
    }

    public EpisodeDocument[] extractEpisodes(String xmlData) throws FeedParsingException {
        try {
            final InputSource inputSource = new InputSource( new StringReader( xmlData ) );
            final SyndFeedInput input = new SyndFeedInput();
            final SyndFeed syndFeed = input.build(inputSource);

            final List<EpisodeDocument> results = new LinkedList<>();
            for(SyndEntry e : syndFeed.getEntries()){
                final EpisodeDocument doc = new EpisodeDocument();

                doc.setTitle(e.getTitle());
                doc.setLink(e.getLink());
                if(e.getPublishedDate() != null){
                    doc.setPubDate(LocalDateTime.ofInstant(e.getPublishedDate().toInstant(), ZoneId.systemDefault()));
                }
                //doc.setGuid(TODO);
                if(e.getDescription() != null){
                    doc.setDescription(e.getDescription().getValue());
                }

                final Module itunesEntryModule = e.getModule("http://www.itunes.com/dtds/podcast-1.0.dtd");
                final EntryInformation itunesEntryInfo = (EntryInformation) itunesEntryModule;

                if(itunesEntryInfo.getImage() != null){
                    doc.setItunesImage(itunesEntryInfo.getImage().toExternalForm());
                }
                if(itunesEntryInfo.getDuration() != null){
                    doc.setItunesDuration(itunesEntryInfo.getDuration().toString());
                }

                results.add(doc);
            }
            return results.toArray(new EpisodeDocument[0]);
        } catch (FeedException e) {
            throw new FeedParsingException("RomeFeedParser could not parse the feed (trying to extract the episodes)", e);
        }
    }

    private List<Link> getAtomLinks(SyndFeed syndFeed){
        final Module atomFeedModule = syndFeed.getModule("http://www.w3.org/2005/Atom");
        final AtomLinkModule atomLinkModule = (AtomLinkModule) atomFeedModule;
        return atomLinkModule.getLinks();
    }



}
