package echo.core.converter;

import echo.core.dto.document.DTO;
import echo.core.dto.document.EpisodeDTO;
import echo.core.dto.document.IndexResult;
import echo.core.dto.document.PodcastDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converters of this type convert documents retrieved of some search
 * index (e.g. Lucene) to Echos internal IndexResult format. For searching,
 * multiple fields may be used to find suitable documents, but only
 * some selected information should be actually retrieved and transfered
 * to the outside (e.g. to be shown in a search result list).
 *
 * To convert index documents to their full information range, use
 * converters extending {@link echo.core.converter.DocumentConverter}
 *
 * @author Maximilian Irro
 */
public class ResultConverter {

    public IndexResult toResult(DTO dto) {

        final IndexResult result = new IndexResult();

        if(dto instanceof PodcastDTO){
            result.setDocType("podcast");
        } else if (dto instanceof EpisodeDTO){
            result.setDocType("episode");
        } else {
            throw new RuntimeException("I forgot to support a new document type : " + dto.getClass());
        }
        if(dto.getDocId()       != null){ result.setEchoId(dto.getDocId()); } // TODO change to echo_id
        if(dto.getTitle()       != null){ result.setTitle(dto.getTitle()); }
        if(dto.getLink()        != null){ result.setLink(dto.getLink()); }
        if(dto.getPubDate()     != null){ result.setPubDate(dto.getPubDate()); }
        if(dto.getDescription() != null){ result.setDescription(dto.getDescription()); }
        if(dto.getItunesImage() != null){ result.setItunesImage(dto.getItunesImage()); }
        //if(lDoc.get("itunes_duration") != null){ result.setItunesDuration(lDoc.get("itunes_duration")); } // TODO

        // note: we do not retrieve websiteData

        return result;
    }

    public List<IndexResult> toResultList(List<DTO> indexList){
        return indexList.stream()
            .map(this::toResult)
            .collect(Collectors.toList());
    }
}