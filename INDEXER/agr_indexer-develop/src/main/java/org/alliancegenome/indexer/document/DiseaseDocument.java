package org.alliancegenome.indexer.document;


import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiseaseDocument extends SearchableItem {

    { category = "disease"; }

    private String doId;
    private String primaryKey;

    private Set<String> parentDiseaseNames;
    private String definition;
    private Date dateProduced;
    private List<String> definitionLinks;
    private List<AnnotationDocument> annotations;
    private List<DiseaseDocument> parents;
    private List<DiseaseDocument> children;
    private List<String> synonyms;
    private List<CrossReferenceDoclet> crossReferences;
    private List<SourceDoclet> sourceList;
    @JsonProperty("disease_group")
    private Set<String> highLevelSlimTermNames = new HashSet<>();

}
