package echo.core.parse.rss;

import com.rometools.modules.atom.modules.AtomLinkModule;
import com.rometools.modules.content.ContentModule;
import com.rometools.modules.itunes.EntryInformation;
import com.rometools.modules.itunes.FeedInformation;
import com.rometools.modules.itunes.types.Category;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import echo.core.domain.dto.immutable.*;
import echo.core.exception.FeedParsingException;
import echo.core.parse.rss.rome.PodloveSimpleChapterItem;
import echo.core.parse.rss.rome.PodloveSimpleChapterModule;
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
public class TestRomeFeedParser implements TestFeedParser {

    private static final Logger log = LoggerFactory.getLogger(RomeFeedParser.class);

    @Override
    public TestPodcast parseFeed(String xmlData) throws FeedParsingException {
        try {
            final InputSource inputSource = new InputSource( new StringReader( xmlData ) );
            final SyndFeedInput input = new SyndFeedInput();
            final SyndFeed feed = input.build(inputSource);

            final ImmutableTestPodcast.Builder builder = ImmutableTestPodcast.builder();

            builder.setTitle(feed.getTitle());
            String link = UrlUtil.sanitize(feed.getLink());
            builder.setLink(link);
            builder.setDescription(feed.getDescription());

            SyndImage img = null;
            if (feed.getImage() != null) {
                img = feed.getImage();
                if(img.getUrl() != null){
                    builder.setImage(img.getUrl());
                }

                // now, it title/link/description were NULL, we use the values set in
                // the image tag as fallbacks because they usually have the same values
                if (feed.getTitle() == null && img.getTitle() != null) {
                    builder.setTitle(img.getTitle());
                }
                if (link == null && img.getLink() != null) {
                    link = UrlUtil.sanitize(img.getLink());
                    builder.setLink(link);
                }
                if (feed.getDescription() == null && img.getDescription() != null) {
                    builder.setDescription(img.getDescription());
                }
            }

            if (feed.getPublishedDate() != null) {
                builder.setPubDate(LocalDateTime.ofInstant(feed.getPublishedDate().toInstant(), ZoneId.systemDefault()));
            }
            builder.setLanguage(feed.getLanguage());
            builder.setGenerator(feed.getGenerator());
            builder.setCopyright(feed.getCopyright());
            builder.setDocs(feed.getDocs());
            builder.setManagingEditor(feed.getManagingEditor());

            // access the <itunes:...> entries
            final Module itunesFeedModule = feed.getModule(FeedInformation.URI);
            final FeedInformation itunes = (FeedInformation) itunesFeedModule;
            if (itunes != null) {
                builder.setItunesSummary(itunes.getSummary());
                builder.setItunesAuthor(itunes.getAuthor());
                builder.setItunesKeywords(String.join(", ", itunes.getKeywords()));

                // we set the itunes image as a fallback only
                if (itunes.getImage() != null) {
                    if (img == null || img.getUrl() == null) {
                        builder.setImage(itunes.getImage().toExternalForm());
                    }
                }
                builder.setItunesCategories(new LinkedHashSet<>(
                    itunes.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toCollection(LinkedList::new))));
                builder.setItunesExplicit(itunes.getExplicit());
                builder.setItunesBlock(itunes.getBlock());
                builder.setItunesType(itunes.getType());
                builder.setItunesOwnerName(itunes.getOwnerName());
                builder.setItunesOwnerEmail(itunes.getOwnerEmailAddress());
                //builder.setItunesCategory(String.join(" | ", itunesFeedInfo.getCategories().stream().map(c->c.getName()).collect(Collectors.toCollection(LinkedList::new))));
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
                } else if(atomLink.getRel().equals("related")) {
                    // TODO
                } else if (atomLink.getRel().equals("prev-archive")) {
                } else {
                    log.warn("Came across an <atom:link> with a relation I do not handle : '{}'", atomLink.getRel());
                }
            }

            return builder.create();

        } catch (FeedException | IllegalArgumentException e) {
            throw new FeedParsingException("RomeFeedParser could not parse the feed", e);
        }
    }

    @Override
    public TestEpisode[] extractEpisodes(String xmlData) throws FeedParsingException {
        try {
            final InputSource inputSource = new InputSource( new StringReader( xmlData ) );
            final SyndFeedInput input = new SyndFeedInput();
            final SyndFeed feed = input.build(inputSource);

            final List<TestEpisode> results = new LinkedList<>();
            for(SyndEntry e : feed.getEntries()){
                //final EpisodeDTO episode = new EpisodeDTO();
                final ImmutableTestEpisode.Builder builder = ImmutableTestEpisode.builder();

                builder.setTitle(e.getTitle());
                builder.setLink(UrlUtil.sanitize(e.getLink()));
                if(e.getPublishedDate() != null){
                    builder.setPubDate(LocalDateTime.ofInstant(e.getPublishedDate().toInstant(), ZoneId.systemDefault()));
                }
                //doc.setGuid(TODO);
                if(e.getDescription() != null){
                    builder.setDescription(e.getDescription().getValue());
                }

                if (e.getUri() != null) {
                    builder.setGuid(e.getUri());
                }

                if(e.getEnclosures() != null && e.getEnclosures().size() > 0){
                    final SyndEnclosure enclosure = e.getEnclosures().get(0);
                    builder.setEnclosureUrl(enclosure.getUrl());
                    builder.setEnclosureType(enclosure.getType());
                    builder.setEnclosureLength(enclosure.getLength());
                    if(e.getEnclosures().size() > 1){
                        log.warn("Encountered multiple <enclosure> elements in <item> element");
                    }
                }

                // access the <content:encoded> entries
                final Module contentModule = e.getModule(ContentModule.URI);
                final ContentModule content = (ContentModule) contentModule;
                if (content != null) {
                    if(content.getEncodeds().size() > 0){
                        builder.setContentEncoded(content.getEncodeds().get(0));
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
                        builder.setImage(itunes.getImage().toExternalForm());
                    }
                    if(itunes.getDuration() != null){
                        builder.setItunesDuration(itunes.getDuration().toString());
                    }
                    builder.setItunesSubtitle(itunes.getSubtitle());
                    builder.setItunesAuthor(itunes.getAuthor());
                    builder.setItunesSummary(itunes.getSummary());
                    builder.setItunesSeason(itunes.getSeason());
                    builder.setItunesEpisode(itunes.getEpisode());
                    builder.setItunesEpisodeType(itunes.getEpisodeType());
                } else {
                    log.debug("No iTunes Namespace elements found in Episode");
                }

                // access the <psc:chapter> entries
                final Module pscEntryModule = e.getModule(PodloveSimpleChapterModule.URI);
                final PodloveSimpleChapterModule simpleChapters = ((PodloveSimpleChapterModule) pscEntryModule);
                if (simpleChapters != null) {
                    if (simpleChapters.getChapters() != null && simpleChapters.getChapters().size() > 0) {
                        final List<TestChapter> chapters = new LinkedList<>();
                        for (PodloveSimpleChapterItem sci : simpleChapters.getChapters()) {
                            final ImmutableTestChapter.Builder c = ImmutableTestChapter.builder();
                            c.setStart(sci.getStart());
                            c.setTitle(sci.getTitle());
                            c.setHref(sci.getHref());
                            chapters.add(c.create());
                        }
                        builder.setChapters(chapters);
                    }
                } else {
                    log.debug("No Podlove Simple Chapter marks found in Episode");
                }

                results.add(builder.create());
            }
            return results.toArray(new TestEpisode[0]);
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

