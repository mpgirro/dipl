package echo.core.parse.rss;

import com.rometools.modules.atom.modules.AtomLinkModule;
import com.rometools.modules.content.ContentModule;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.modules.itunes.types.Category;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import echo.core.domain.dto.EpisodeDTO;
import echo.core.domain.dto.PodcastDTO;
import echo.core.domain.feed.Chapter;
import echo.core.exception.FeedParsingException;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.rome.feed.atom.Link;
import echo.core.parse.rss.rome.PodloveSimpleChapterModule;
import echo.core.parse.rss.rome.SimpleChapter;
import echo.core.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Maximilian Irro
 */
public class RomeFeedParser implements FeedParser {

    private static final Logger log = LoggerFactory.getLogger(RomeFeedParser.class);

    @Override
    public PodcastDTO parseFeed(String xmlData) throws FeedParsingException {
        try {
            final InputSource inputSource = new InputSource( new StringReader( xmlData ) );
            final SyndFeedInput input = new SyndFeedInput();
            final SyndFeed feed = input.build(inputSource);

            final PodcastDTO podcast = new PodcastDTO();

            podcast.setTitle(feed.getTitle());
            podcast.setLink(UrlUtil.sanitize(feed.getLink()));
            podcast.setDescription(feed.getDescription());
            if(feed.getPublishedDate() != null){
                podcast.setPubDate(LocalDateTime.ofInstant(feed.getPublishedDate().toInstant(), ZoneId.systemDefault()));
            }
            podcast.setLanguage(feed.getLanguage());
            podcast.setGenerator(feed.getGenerator());
            podcast.setCopyright(feed.getCopyright());
            podcast.setDocs(feed.getDocs());
            podcast.setManagingEditor(feed.getManagingEditor());

            // access the <itunes:...> entries
            final Module itunesFeedModule = feed.getModule(FeedInformation.URI);
            final FeedInformation itunes = (FeedInformation) itunesFeedModule;
            if(itunes != null){
                podcast.setItunesSummary(itunes.getSummary());
                podcast.setItunesAuthor(itunes.getAuthor());
                podcast.setItunesKeywords(String.join(", ", itunes.getKeywords()));
                if(itunes.getImage() != null){
                    podcast.setItunesImage(itunes.getImage().toExternalForm());
                }
                podcast.setItunesCategories(new LinkedHashSet<>(
                    itunes.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toCollection(LinkedList::new))));
                podcast.setItunesExplicit(itunes.getExplicit());
                podcast.setItunesBlock(itunes.getBlock());
                podcast.setItunesType(itunes.getType());
                podcast.setItunesOwnerName(itunes.getOwnerName());
                podcast.setItunesOwnerEmail(itunes.getOwnerEmailAddress());
                //podcast.setItunesCategory(String.join(" | ", itunesFeedInfo.getCategories().stream().map(c->c.getName()).collect(Collectors.toCollection(LinkedList::new))));
            } else {
                log.debug("No iTunes Namespace elements found in Podcast");
            }

            // here I process the feed specific atom Links
            final List<Link> atomLinks = getAtomLinks(feed);
            for(Link atomLink : atomLinks){
                if(atomLink.getRel().equals("http://podlove.org/deep-link")){
                    // TODO this should be a link to the episode website (but is it always though?!)
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
                } else if(atomLink.getRel().equals("search")){
                    // TODO
                } else if(atomLink.getRel().equals("via")) {
                    // TODO
                } else if(atomLink.getRel().equals("related")){
                    // TODO
                } else {
                    log.warn("Came across an <atom:link> with a relation I do not handle: '{}'", atomLink.getRel());
                }
            }

            return podcast;

        } catch (FeedException | IllegalArgumentException e) {
            throw new FeedParsingException("RomeFeedParser could not parse the feed", e);
        }
    }

    @Override
    public EpisodeDTO[] extractEpisodes(String xmlData) throws FeedParsingException {
        try {
            final InputSource inputSource = new InputSource( new StringReader( xmlData ) );
            final SyndFeedInput input = new SyndFeedInput();
            final SyndFeed feed = input.build(inputSource);

            final List<EpisodeDTO> results = new LinkedList<>();
            for(SyndEntry e : feed.getEntries()){
                final EpisodeDTO episode = new EpisodeDTO();

                episode.setTitle(e.getTitle());
                episode.setLink(UrlUtil.sanitize(e.getLink()));
                if(e.getPublishedDate() != null){
                    episode.setPubDate(LocalDateTime.ofInstant(e.getPublishedDate().toInstant(), ZoneId.systemDefault()));
                }
                //doc.setGuid(TODO);
                if(e.getDescription() != null){
                    episode.setDescription(e.getDescription().getValue());
                }

                if (e.getUri() != null) {
                    episode.setGuid(e.getUri());
                }

                if(e.getEnclosures() != null && e.getEnclosures().size() > 0){
                    final SyndEnclosure enclosure = e.getEnclosures().get(0);
                    episode.setEnclosureUrl(enclosure.getUrl());
                    episode.setEnclosureType(enclosure.getType());
                    episode.setEnclosureLength(enclosure.getLength());
                    if(e.getEnclosures().size() > 1){
                        log.warn("Encountered multiple <enclosure> elements in <item> element");
                    }
                }

                // access the <content:encoded> entries
                final Module contentModule = e.getModule(ContentModule.URI);
                final ContentModule content = (ContentModule) contentModule;
                if (content != null) {
                    if(content.getEncodeds().size() > 0){
                        episode.setContentEncoded(content.getEncodeds().get(0));
                        if(content.getEncodeds().size() > 1){
                            log.warn("Encountered multiple <content:encoded> elements in <item> element");
                        }
                    }
                }

                // access the <itunes:...> entries
                final Module itunesEntryModule = e.getModule(EntryInformation.URI);
                final EntryInformation itunes = (EntryInformation) itunesEntryModule;
                if (itunes != null) {
                    if(itunes.getImage() != null){
                        episode.setItunesImage(itunes.getImage().toExternalForm());
                    }
                    if(itunes.getDuration() != null){
                        episode.setItunesDuration(itunes.getDuration().toString());
                    }
                    episode.setItunesSubtitle(itunes.getSubtitle());
                    episode.setItunesAuthor(itunes.getAuthor());
                    episode.setItunesSummary(itunes.getSummary());
                    episode.setItunesSeason(itunes.getSeason());
                    episode.setItunesEpisode(itunes.getEpisode());
                    episode.setItunesEpisodeType(itunes.getEpisodeType());
                } else {
                    log.debug("No iTunes Namespace elements found in Episode");
                }

                // access the <psc:chapter> entries
                final Module pscEntryModule = e.getModule(PodloveSimpleChapterModule.URI);
                final PodloveSimpleChapterModule simpleChapters = ((PodloveSimpleChapterModule) pscEntryModule);
                if (simpleChapters != null) {
                    if (simpleChapters.getChapters() != null && simpleChapters.getChapters().size() > 0) {
                        final List<Chapter> chapters = new LinkedList<>();
                        for (SimpleChapter sc : simpleChapters.getChapters()) {
                            final Chapter c = new Chapter();
                            c.setStart(sc.getStart());
                            c.setTitle(sc.getTitle());
                            chapters.add(c);
                        }
                        episode.setChapters(chapters);
                    }
                }

                results.add(episode);
            }
            return results.toArray(new EpisodeDTO[0]);
        } catch (FeedException | IllegalArgumentException e) {
            throw new FeedParsingException("RomeFeedParser could not parse the feed (trying to extract the episodes)", e);
        }
    }

    private List<Link> getAtomLinks(SyndFeed syndFeed){
        final Module atomFeedModule = syndFeed.getModule(AtomLinkModule.URI);
        final AtomLinkModule atomLinkModule = (AtomLinkModule) atomFeedModule;
        if(atomLinkModule != null){
            return atomLinkModule.getLinks();
        }
        return new LinkedList<>();
    }

}
