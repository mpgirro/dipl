package echo.core.converter;

import echo.core.dto.DTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the generic converter between Echo {@link DTO}'s
 * and documents of type I of the specific search index (e.g. org.apache.lucene.document.Document
 * or org.apache.solr.common.SolrInputDocument)
 *
 * @author Maximilian Irro
 */
public abstract class DocumentConverter<E extends DTO, I> {

    public abstract E toDTO(I indexDoc);

    public abstract I toIndex(E echoDoc);

    public List<E> toDTOList(List<I> indexList){
        return indexList.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<I> toIndexList(List<E> echoList){
        return echoList.stream()
            .map(this::toIndex)
            .collect(Collectors.toList());
    }

}
