package echo.core.converter;

import echo.core.dto.document.IndexResult;

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
public abstract class ResultConverter<I> {

    public abstract IndexResult toResult(I indexDoc);

    public List<IndexResult> toResultList(List<I> indexList){
        return indexList.stream()
            .map(this::toResult)
            .collect(Collectors.toList());
    }
}
