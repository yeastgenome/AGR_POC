package org.alliancegenome.indexer.translators;

import org.alliancegenome.indexer.document.*;
import org.alliancegenome.indexer.entity.node.*;
import org.alliancegenome.indexer.entity.relationship.GenomeLocation;
import org.alliancegenome.indexer.entity.relationship.Orthologous;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GeneTranslator extends EntityDocumentTranslator<Gene, GeneDocument> {

    private final Logger log = LogManager.getLogger(getClass());

    private static DiseaseTranslator diseaseTranslator = new DiseaseTranslator();
    private static FeatureTranslator alleleTranslator = new FeatureTranslator();

    @Override
    protected GeneDocument entityToDocument(Gene entity, int translationDepth) {
        //log.info(entity);
        HashMap<String, ArrayList<String>> goTerms = new HashMap<>();

        GeneDocument geneDocument = new GeneDocument();

        geneDocument.setCategory("gene");

        geneDocument.setDataProvider(entity.getDataProvider());
        geneDocument.setDescription(entity.getDescription());

        geneDocument.setGeneLiteratureUrl(entity.getGeneLiteratureUrl());
        geneDocument.setGeneSynopsis(entity.getGeneSynopsis());
        geneDocument.setGeneSynopsisUrl(entity.getGeneSynopsisUrl());
        geneDocument.setGeneticEntityExternalUrl(entity.getGeneticEntityExternalUrl());

        geneDocument.setModCrossRefCompleteUrl(entity.getModCrossRefCompleteUrl());
        geneDocument.setModLocalId(entity.getModLocalId());
        geneDocument.setModGlobalCrossRefId(entity.getModGlobalCrossRefId());
        geneDocument.setModGlobalId(entity.getModGlobalId());
        if (entity.getName() == null)
            geneDocument.setName(entity.getSymbol());
        else
            geneDocument.setName(entity.getName());
        geneDocument.setNameKeyWithSpecies(entity.getSymbol(), entity.getSpecies().getType().getAbbreviation());
        geneDocument.setPrimaryId(entity.getPrimaryKey());
        geneDocument.setDateProduced(entity.getDateProduced());
        geneDocument.setTaxonId(entity.getTaxonId());


        if (entity.getCreatedBy() != null) {
            geneDocument.setRelease(entity.getCreatedBy().getRelease());
        }
        if (entity.getSpecies() != null) {
            geneDocument.setSpecies(entity.getSpecies().getName());
        }

        // Setup Go Terms by type
        for (GOTerm term : entity.getGOTerms()) {
            ArrayList<String> list = goTerms.get(term.getType());
            if (list == null) {
                list = new ArrayList<>();
                goTerms.put(term.getType(), list);
            }
            if (!list.contains(term.getName())) {
                list.add(term.getName());
            }
        }
        geneDocument.setGene_biological_process(goTerms.get("biological_process"));
        geneDocument.setGene_cellular_component(goTerms.get("cellular_component"));
        geneDocument.setGene_molecular_function(goTerms.get("molecular_function"));

        // This code is duplicated in Gene and Feature should be pulled out into its own translator
        ArrayList<String> secondaryIds = new ArrayList<>();
        if (entity.getSecondaryIds() != null) {
            for (SecondaryId secondaryId : entity.getSecondaryIds()) {
                secondaryIds.add(secondaryId.getName());
            }
        }
        geneDocument.setSecondaryIds(secondaryIds);


        if (entity.getSOTerm() != null) {
            geneDocument.setSoTermId(entity.getSOTerm().getPrimaryKey());
            geneDocument.setSoTermName(entity.getSOTerm().getName());
        }
        geneDocument.setSymbol(entity.getSymbol());

        // This code is duplicated in Gene and Feature should be pulled out into its own translator
        ArrayList<String> synonyms = new ArrayList<>();
        if (entity.getSynonyms() != null) {
            for (Synonym synonym : entity.getSynonyms()) {
                if (synonym.getPrimaryKey() != null) {
                    synonyms.add(synonym.getPrimaryKey());
                } else {
                    synonyms.add(synonym.getName());
                }
            }
        }
        geneDocument.setSynonyms(synonyms);


//      if(entity.getOrthoGenes() != null) {
//      if(lookup.size() + entity.getOrthologyGeneJoins().size() > 0) {
//          System.out.println(lookup.size() + " ==? " + entity.getOrthologyGeneJoins().size());
//      }

        if (entity.getOrthologyGeneJoins().size() > 0 && translationDepth > 0) {
            List<OrthologyDoclet> olist = new ArrayList<>();

            HashMap<String, Orthologous> lookup = new HashMap<>();
            if (entity.getOrthoGenes() != null)
                for (Orthologous o : entity.getOrthoGenes()) {
                    lookup.put(o.getPrimaryKey(), o);
                }

            for (OrthologyGeneJoin join : entity.getOrthologyGeneJoins()) {

                if (lookup.containsKey(join.getPrimaryKey())) {

                    ArrayList<String> matched = new ArrayList<String>();
                    if (join.getMatched() != null) {
                        for (OrthoAlgorithm algo : join.getMatched()) {
                            matched.add(algo.getName());
                        }
                    }
                    ArrayList<String> notMatched = new ArrayList<String>();
                    if (join.getNotMatched() != null) {
                        for (OrthoAlgorithm algo : join.getNotMatched()) {
                            notMatched.add(algo.getName());
                        }
                    }
                    ArrayList<String> notCalled = new ArrayList<String>();
                    if (join.getNotCalled() != null) {
                        for (OrthoAlgorithm algo : join.getNotCalled()) {
                            notCalled.add(algo.getName());
                        }
                    }
                    Orthologous orth = lookup.get(join.getPrimaryKey());
                    OrthologyDoclet doc = new OrthologyDoclet(
                            orth.getPrimaryKey(),
                            orth.isBestScore(),
                            orth.isBestRevScore(),
                            orth.getConfidence(),
                            orth.getGene1().getSpecies() == null ? null : orth.getGene1().getSpecies().getPrimaryKey(),
                            orth.getGene2().getSpecies() == null ? null : orth.getGene2().getSpecies().getPrimaryKey(),
                            orth.getGene1().getSpecies() == null ? null : orth.getGene1().getSpecies().getName(),
                            orth.getGene2().getSpecies() == null ? null : orth.getGene2().getSpecies().getName(),
                            orth.getGene1().getSymbol(),
                            orth.getGene2().getSymbol(),
                            orth.getGene1().getPrimaryKey(),
                            orth.getGene2().getPrimaryKey(),
                            notCalled, matched, notMatched
                    );
                    olist.add(doc);
                }

            }
            geneDocument.setOrthology(olist);
        }

        if (entity.getDiseaseEntityJoins() != null && translationDepth > 0) {
            List<DiseaseDocument> diseaseList = diseaseTranslator.getDiseaseDocuments(entity, entity.getDiseaseEntityJoins(), translationDepth);
            geneDocument.setDiseases(diseaseList);
        }

        if (entity.getGenomeLocations() != null) {
            List<GenomeLocationDoclet> gllist = new ArrayList<>();
            for (GenomeLocation location : entity.getGenomeLocations()) {
                GenomeLocationDoclet loc = new GenomeLocationDoclet(
                        location.getStart(),
                        location.getEnd(),
                        location.getAssembly(),
                        location.getStrand(),
                        location.getChromosome().getPrimaryKey());

                gllist.add(loc);
            }
            geneDocument.setGenomeLocations(gllist);
        }

        if (entity.getCrossReferences() != null) {
            List<CrossReferenceDoclet> crlist = entity.getCrossReferences().stream()
                    .map(crossReference -> {
                        CrossReferenceDoclet crd = new CrossReferenceDoclet();
                        crd.setCrossRefCompleteUrl(crossReference.getCrossRefCompleteUrl());
                        crd.setName(crossReference.getName());
                        crd.setGlobalCrossRefId(crossReference.getGlobalCrossRefId());
                        crd.setLocalId(crossReference.getLocalId());
                        crd.setPrefix(crossReference.getPrefix());
                        crd.setPrimaryKey(crossReference.getPrimaryKey());
                        return crd;
                    })
                    .collect(Collectors.toList());
            geneDocument.setCrossReferences(crlist);
        }

        if (entity.getFeatures() != null && translationDepth > 0) {
            List<FeatureDocument> featureList = new ArrayList<>();
            entity.getFeatures().forEach(feature ->
                    featureList.add(alleleTranslator.entityToDocument(feature, translationDepth - 1))
            );
            geneDocument.setAlleles(featureList);
        }

        return geneDocument;
    }

    @Override
    protected Gene documentToEntity(GeneDocument document, int translationDepth) {
        // We are not going to the database yet so will implement this when we need to
        return null;
    }

}
