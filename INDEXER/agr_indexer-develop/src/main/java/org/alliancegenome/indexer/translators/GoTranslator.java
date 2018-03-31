package org.alliancegenome.indexer.translators;

import java.util.ArrayList;

import org.alliancegenome.indexer.document.GoDocument;
import org.alliancegenome.indexer.entity.node.GOTerm;
import org.alliancegenome.indexer.entity.node.Gene;
import org.alliancegenome.indexer.entity.node.Synonym;

public class GoTranslator extends EntityDocumentTranslator<GOTerm, GoDocument> {


    @Override
    protected GoDocument entityToDocument(GOTerm entity, int translationDepth) {
        //log.info(entity);
        GoDocument doc = new GoDocument();

        doc.setName(entity.getName());
        doc.setHref(entity.getHref());
        doc.setPrimaryId(entity.getPrimaryKey());
        doc.setNameKey(entity.getNameKey());
        doc.setGo_type(entity.getType());
        doc.setDescription(entity.getDescription());

        ArrayList<String> go_synonyms = new ArrayList<>();
        for(Synonym s: entity.getSynonyms()) {
            go_synonyms.add(s.getPrimaryKey());
        }
        doc.setSynonyms(go_synonyms);

        ArrayList<String> go_species = new ArrayList<>();
        ArrayList<String> go_genes = new ArrayList<String>();
        for(Gene g: entity.getGenes()) {
            if(g.getSpecies() != null && g.getSpecies().getSpecies() != null && !go_species.contains(g.getSpecies().getSpecies())) {
                go_species.add(g.getSpecies().getSpecies());
            }
            if(g.getSymbol() != null) {
                go_genes.add(g.getSymbol());
            }
        }
        doc.setGo_genes(go_genes);
        doc.setGo_species(go_species);

        return doc;
    }

    @Override
    protected GOTerm documentToEntity(GoDocument document, int translationDepth) {
        return null;
    }

}
