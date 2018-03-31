package org.alliancegenome.indexer.indexers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alliancegenome.indexer.config.ConfigHelper;
import org.alliancegenome.indexer.config.IndexerConfig;
import org.alliancegenome.indexer.document.ESDocument;
import org.alliancegenome.indexer.schema.Mappings;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public abstract class Indexer<D extends ESDocument> extends Thread {

    private Logger log = LogManager.getLogger(getClass());
    protected String currentIndex;
    protected IndexerConfig indexerConfig;
    private PreBuiltXPackTransportClient client;
    protected Runtime runtime = Runtime.getRuntime();
    protected DecimalFormat df = new DecimalFormat("#");
    protected ObjectMapper om = new ObjectMapper();

    // Used for showing progress
    private Date startTime = new Date();
    private Date lastTime = new Date();
    private int lastSize;

    public Indexer(String currentIndex, IndexerConfig indexerConfig) {
        this.currentIndex = currentIndex;
        this.indexerConfig = indexerConfig;

        om.setSerializationInclusion(Include.NON_NULL);

        try {
            client = new PreBuiltXPackTransportClient(Settings.EMPTY);
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ConfigHelper.getEsHost()), ConfigHelper.getEsPort()));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    protected abstract void index();

    public void runIndex() {
        addMapping();
        index();
    }

    private void addMapping() {
        try {
            Mappings mappingClass = (Mappings) indexerConfig.getMappingsClazz().getDeclaredConstructor(Boolean.class).newInstance(true);
            mappingClass.buildMappings();
            log.debug("Getting Mapping for type: " + indexerConfig.getTypeName());
            client.admin().indices().preparePutMapping(currentIndex).setType(indexerConfig.getTypeName()).setSource(mappingClass.getBuilder().string()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        addMapping();
        index();
    }

    public void saveDocuments(Iterable<D> docs) {
        checkMemory();

        if (((Collection<D>) docs).size() > 0) {

            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (D doc : docs) {
                try {
                    String json = om.writeValueAsString(doc);
                    //log.debug("JSON: " + json);
                    bulkRequest.add(client.prepareIndex(currentIndex, indexerConfig.getTypeName()).setSource(json).setId(doc.getDocumentId()));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                log.error("Has Failures in indexer: " + bulkResponse.buildFailureMessage());
                // process failures by iterating through each bulk response item
            }
        }

    }

    // Used to show progress when using a queue
    private void startProcess(int totalDocAmount) {
        log.info("Start Indexing: queue size: " + getBigNumber(totalDocAmount));
        lastTime = new Date();
        lastSize = totalDocAmount;
    }

    // Used to show process when using a queue
    private void progress(int currentSize, int totalDocAmount) {
        double percent = ((double) (totalDocAmount - currentSize) / (double) totalDocAmount);
        Date now = new Date();
        long diff = now.getTime() - startTime.getTime();
        long time = (now.getTime() - lastTime.getTime());
        int processedAmount = (lastSize - currentSize);
        String message = "" + getBigNumber(totalDocAmount - currentSize) + " records [" + getBigNumber(totalDocAmount) + "] " + (int) (percent * 100) + "% took: " + (time / 1000) + "s to process " + processedAmount + " records at a rate of: " + ((processedAmount * 1000) / time) + "r/s";
        if (percent > 0) {
            int perms = (int) (diff / percent);
            Date end = new Date(startTime.getTime() + perms);
            message += ", Memory: " + df.format(memoryPercent() * 100) + "%, Estimated Finish: " + end;
        }
        log.info(message);
        lastSize = currentSize;
        lastTime = now;
    }

    // Used to show progress when using batches
    protected void startProcess(int amountBatches, int batchSize, int totalDocAmount) {
        log.info("Starting Processing: batches: " + amountBatches + " size: " + batchSize + " total: " + getBigNumber(totalDocAmount) + " at: " + startTime);
        lastTime = new Date();
    }

    // Used to show progress when using batches
    protected void progress(int currentBatch, int totalBatches, int processedAmount) {
        double percent = ((double) currentBatch / (double) totalBatches);
        Date now = new Date();
        long diff = now.getTime() - startTime.getTime();
        long time = (now.getTime() - lastTime.getTime());
        if (percent > 0) {
            int perms = (int) (diff / percent);
            Date end = new Date(startTime.getTime() + perms);
            log.info("Batch: " + currentBatch + " of " + totalBatches + " took: " + time + "ms to process " + processedAmount + " records at a rate of: " + ((processedAmount * 1000) / time) + "r/s, Memory: " + df.format(memoryPercent() * 100) + "%, Percentage complete: " + (int) (percent * 100) + "%, Estimated Finish: " + end);
        } else {
            log.info("Batch: " + currentBatch + " of " + totalBatches + " took: " + time + "ms to process " + processedAmount + " records at a rate of: " + ((processedAmount * 1000) / time) + "r/s");
        }
        lastTime = now;
    }

    private void finishProcess(int totalDocAmount) {
        Date now = new Date();
        long duration = now.getTime() - startTime.getTime();
        String result = getHumanReadableTimeDisplay(duration);
        log.info("Finished: took: " + result + " to process " + getBigNumber(totalDocAmount) + " records at a rate of: " + ((totalDocAmount * 1000) / duration) + "r/s");
    }

    public static String getBigNumber(int number) {
        return String.format("%,d", number);
    }

    public static String getHumanReadableTimeDisplay(long duration) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void checkMemory() {
        if (memoryPercent() > 0.95) {
            log.warn("Memory Warning: " + df.format(memoryPercent() * 100) + "%");
            log.warn("Used Mem: " + (runtime.totalMemory() - runtime.freeMemory()));
            log.warn("Free Mem: " + runtime.freeMemory());
            log.warn("Total Mem: " + runtime.totalMemory());
            log.warn("Max Memory: " + runtime.maxMemory());
        }
    }

    private double memoryPercent() {
        return ((double) runtime.totalMemory() - (double) runtime.freeMemory()) / (double) runtime.maxMemory();
    }

    private boolean isWorkStillPerformed(LinkedBlockingDeque<String> queue, Set<Future> futureSet) {
        // check if at least one thread is still working, i.e. is not done
        boolean atLeastOneThreadRunning = false;
        for (Future future : futureSet) {
            if (!future.isDone()) {
                atLeastOneThreadRunning = true;
                break;
            }
        }
        return !queue.isEmpty() && atLeastOneThreadRunning;
    }

    private BasicThreadFactory getBasicThreadFactory() {
        // Create a factory that produces daemon threads with a naming pattern and
        // a priority
        return new BasicThreadFactory.Builder()
                .namingPattern("AGR-Indexer-%d")
                .priority(Thread.MAX_PRIORITY)
                .build();
    }

    void initiateThreading(LinkedBlockingDeque<String> queue) throws InterruptedException {
        Integer numberOfThreads = indexerConfig.getThreadCount();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads, getBasicThreadFactory());
        int index = 0;
        Set<Future> futureSet = new HashSet<>(numberOfThreads);
        while (index++ < numberOfThreads) {
            futureSet.add(executor.submit(() -> startSingleThread(queue)));
        }

        int total = queue.size();
        startProcess(total);
        while (isWorkStillPerformed(queue, futureSet)) {
            TimeUnit.SECONDS.sleep(10);
            progress(queue.size(), total);
        }
        if (!queue.isEmpty())
            throw new RuntimeException("There was an error during the multi-threaded indexing. Aborting...");
        finishProcess(total);
        executor.shutdown();
    }

    protected abstract void startSingleThread(LinkedBlockingDeque<String> queue);

}
