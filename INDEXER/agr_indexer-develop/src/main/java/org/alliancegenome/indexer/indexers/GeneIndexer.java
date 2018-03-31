package org.alliancegenome.indexer.indexers;

import org.alliancegenome.indexer.config.IndexerConfig;
import org.alliancegenome.indexer.document.GeneDocument;
import org.alliancegenome.indexer.entity.node.Gene;
import org.alliancegenome.indexer.repository.GeneRepository;
import org.alliancegenome.indexer.translators.GeneTranslator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class GeneIndexer extends Indexer<GeneDocument> {

    private final Logger log = LogManager.getLogger(getClass());

    public GeneIndexer(String currentIndex, IndexerConfig config) {
        super(currentIndex, config);
    }

    @Override
    public void index() {
        try {
            LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<>();
            GeneRepository geneRepo = new GeneRepository();
            List<String> fulllist = geneRepo.getAllGeneKeys();
            queue.addAll(fulllist);
            geneRepo.clearCache();
            initiateThreading(queue);
        } catch (InterruptedException e) {
            log.error("Error while indexing...", e);
        }

    }

    protected void startSingleThread(LinkedBlockingDeque<String> queue) {
        ArrayList<Gene> list = new ArrayList<>();
        GeneRepository repo = new GeneRepository();
        GeneTranslator geneTrans = new GeneTranslator();
        while (true) {
            try {
                if (list.size() >= indexerConfig.getBufferSize()) {
                    saveDocuments(geneTrans.translateEntities(list));
                    list.clear();
                    repo.clearCache();
                    list = new ArrayList<>();
                }
                if (queue.isEmpty()) {
                    if (list.size() > 0) {
                        saveDocuments(geneTrans.translateEntities(list));
                        repo.clearCache();
                        list.clear();
                    }
                    return;
                }

                String key = queue.takeFirst();
                Gene gene = repo.getOneGene(key);
                if (gene != null)
                    list.add(gene);
                else
                    log.debug("No gene found for " + key);
            } catch (Exception e) {
                log.error("Error while indexing...", e);
                System.exit(-1);
                return;
            }
        }
    }

}