package org.alliancegenome.indexer.document;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnotationDocument extends ESDocument implements Comparable<AnnotationDocument> {

    private String primaryKey;
    private String associationType;
    private SourceDoclet source;
    private GeneDocument geneDocument;
    private FeatureDocument featureDocument;
    private List<PublicationDoclet> publications;

    @JsonIgnore
    public String getDocumentId() {
        return primaryKey;
    }

    @Override
    public int compareTo(AnnotationDocument doc) {
        return 0;
    }
}
