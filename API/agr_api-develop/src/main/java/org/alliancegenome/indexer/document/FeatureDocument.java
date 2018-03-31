package org.alliancegenome.indexer.document;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class FeatureDocument {

    private String category = "allele";
    private String primaryKey;
    private String symbol;
    private String name;
    private Date dateProduced;
    private Date dataProvider;
    private String release;
    private String localId;
    private String globalId;
    private String modCrossRefFullUrl;
    private boolean searchable;

    private List<String> secondaryIds;
    private List<String> synonyms;
    private GeneDocument geneDocument;
    private List<DiseaseDocument> diseaseDocuments = new ArrayList<>();

}
