package echo.common.converter;

import echo.common.dto.document.Document;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the generic converter between Echo {@link echo.common.dto.document.Document}'s
 * and documents of type I of the specific search index (e.g. org.apache.lucene.document.Document
 * or org.apache.solr.common.SolrInputDocument)
 *
 * @author Maximilian Irro
 */
public abstract class DocumentConverter<E extends Document, I> {

    public abstract E toEchoDocument(I indexDoc);

    public abstract I toIndexDocument(E echoDoc);

    public List<E> toEchoList(List<I> indexList){
        return indexList.stream()
            .map(this::toEchoDocument)
            .collect(Collectors.toList());
    }

    public List<I> toIndexList(List<E> echoList){
        return echoList.stream()
            .map(this::toIndexDocument)
            .collect(Collectors.toList());
    }

}
