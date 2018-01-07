package echo.core.converter;

import echo.core.dto.document.Document;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the generic converter between Echo {@link echo.core.dto.document.Document}'s
 * and documents of type I of the specific search index (e.g. org.apache.lucene.document.Document
 * or org.apache.solr.common.SolrInputDocument)
 *
 * @author Maximilian Irro
 */
public abstract class DocumentConverter<E extends Document, I> {

    public abstract E toEchoDocument(I indexDoc);

    public abstract I toEntityDocument(E echoDoc);

    public List<E> toEchoList(List<I> indexList){
        return indexList.stream()
            .map(this::toEchoDocument)
            .collect(Collectors.toList());
    }

    public List<I> toEntityList(List<E> echoList){
        return echoList.stream()
            .map(this::toEntityDocument)
            .collect(Collectors.toList());
    }

}
