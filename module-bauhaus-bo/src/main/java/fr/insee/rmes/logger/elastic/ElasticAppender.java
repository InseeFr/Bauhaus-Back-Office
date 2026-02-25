package fr.insee.rmes.logger.elastic;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Logback appender that sends log events directly to an Elasticsearch backend
 * via the Bulk API, targeting Elasticsearch data streams.
 *
 * <p>Log events are encoded using the configured {@link Encoder} (typically
 * {@code StructuredLogEncoder} with ECS format) and sent in bulk batches to a
 * data stream named {@code {type}-{dataset}-{namespace}}.</p>
 *
 * <h3>Configuration example (logback.xml):</h3>
 * <pre>{@code
 * <appender name="ELASTIC" class="fr.insee.rmes.logger.elastic.ElasticAppender">
 *     <encoder class="org.springframework.boot.logging.logback.StructuredLogEncoder">
 *         <format>ecs</format>
 *     </encoder>
 *
 *     <!-- Required: Elasticsearch endpoint URL (without trailing /) -->
 *     <url>https://my-cluster.es.us-central1.gcp.cloud.es.io:443</url>
 *
 *     <!-- Data stream configuration (defaults to logs-generic-default) -->
 *     <dataStreamType>logs</dataStreamType>
 *     <dataStreamDataset>myapp</dataStreamDataset>
 *     <dataStreamNamespace>production</dataStreamNamespace>
 *
 *     <!-- Optional tuning parameters -->
 *     <batchSize>100</batchSize>
 *     <flushIntervalMillis>5000</flushIntervalMillis>
 *     <maxQueueSize>10000</maxQueueSize>
 *     <connectTimeoutMillis>5000</connectTimeoutMillis>
 *     <readTimeoutMillis>30000</readTimeoutMillis>
 *     <maxRetries>3</maxRetries>
 * </appender>
 * }</pre>
 *
 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html">Elasticsearch Bulk API</a>
 */
public class ElasticAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    // ─── Configuration Fields (settable from logback.xml) ────────────────────────

    /** Elasticsearch endpoint URL (e.g. https://my-cluster.es.cloud.es.io:443) */
    private String url;

    /** Data stream type (default: logs) */
    private String dataStreamType = "logs";

    /** Data stream dataset (default: generic) */
    private String dataStreamDataset = "generic";

    /** Data stream namespace (default: default) */
    private String dataStreamNamespace = "default";

    /** Maximum number of events to batch before sending (default: 100) */
    private int batchSize = 100;

    /** Maximum time in milliseconds between flushes (default: 5000ms) */
    private long flushIntervalMillis = 5000;

    /** Maximum number of events in the queue before dropping (default: 10000) */
    private int maxQueueSize = 10000;

    /** Connection timeout in milliseconds (default: 5000ms) */
    private int connectTimeoutMillis = 5000;

    /** Read timeout in milliseconds (default: 30000ms) */
    private int readTimeoutMillis = 30000;

    /** Number of retry attempts on failure (default: 3) */
    private int maxRetries = 3;

    /** Encoder responsible for formatting each log event (e.g. StructuredLogEncoder with ECS format) */
    private Encoder<ILoggingEvent> encoder;

    // ─── Internal State ──────────────────────────────────────────────────────────

    private BlockingQueue<ILoggingEvent> eventQueue;
    private Thread workerThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private String bulkUrl;

    // ─── Logback Lifecycle ───────────────────────────────────────────────────────

    @Override
    public void start() {
        if (url == null || url.isEmpty()) {
            addError("Elasticsearch URL must be configured for ElasticAppender");
            return;
        }

        if (encoder == null) {
            addError("An encoder must be configured for ElasticAppender");
            return;
        }

        // Build the bulk endpoint URL targeting the data stream
        String dataStreamName = dataStreamType + "-" + dataStreamDataset + "-" + dataStreamNamespace;
        bulkUrl = url.endsWith("/") ? url + dataStreamName + "/_bulk" : url + "/" + dataStreamName + "/_bulk";

        encoder.start();

        eventQueue = new LinkedBlockingQueue<>(maxQueueSize);
        running.set(true);

        workerThread = new Thread(this::workerLoop, "elastic-appender-worker");
        workerThread.setDaemon(true);
        workerThread.start();

        super.start();
        addInfo("ElasticAppender started — bulk endpoint: " + bulkUrl);
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }

        running.set(false);

        // Interrupt the worker and wait for it to finish
        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(flushIntervalMillis + 2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                addWarn("Interrupted while waiting for worker thread to stop");
            }
        }

        encoder.stop();

        super.stop();
        addInfo("ElasticAppender stopped");
    }

    // ─── Append (called by logback on each log event) ────────────────────────────

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }

        // Prepare the event eagerly (capture MDC, etc.)
        eventObject.prepareForDeferredProcessing();

        if (!eventQueue.offer(eventObject)) {
            addWarn("ElasticAppender queue is full (size=" + maxQueueSize + "), dropping log event");
        }
    }

    // ─── Worker Thread ───────────────────────────────────────────────────────────

    /**
     * Background worker loop that drains the event queue and sends bulk requests
     * to Elasticsearch.
     */
    private void workerLoop() {
        List<ILoggingEvent> batch = new ArrayList<>(batchSize);

        while (running.get() || !eventQueue.isEmpty()) {
            try {
                batch.clear();

                // Wait for the first event (with timeout for periodic flushing)
                ILoggingEvent event = eventQueue.poll(flushIntervalMillis, TimeUnit.MILLISECONDS);
                if (event != null) {
                    batch.add(event);
                    // Drain up to batchSize events
                    eventQueue.drainTo(batch, batchSize - 1);
                }

                if (!batch.isEmpty()) {
                    sendBulkRequest(batch);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Drain remaining events before exiting
                batch.clear();
                eventQueue.drainTo(batch);
                if (!batch.isEmpty()) {
                    sendBulkRequest(batch);
                }
                break;
            }
        }
    }

    // ─── Bulk Request ────────────────────────────────────────────────────────────

    /**
     * Sends a batch of log events to Elasticsearch using the Bulk API.
     * Data streams require the {@code create} action for each document.
     */
    private void sendBulkRequest(List<ILoggingEvent> events) {
        String ndjsonBody = buildNdjsonBody(events);
        if (ndjsonBody == null || ndjsonBody.isEmpty()) {
            return;
        }

        int retries = 0;
        while (retries <= maxRetries) {
            try {
                int responseCode = doHttpPost(ndjsonBody);
                if (responseCode >= 200 && responseCode < 300) {
                    return; // Success
                } else if (responseCode == 429 || responseCode >= 500) {
                    // Retryable error (rate limiting or server error)
                    retries++;
                    if (retries <= maxRetries) {
                        long backoff = (long) Math.pow(2, retries) * 250; // Exponential backoff
                        addWarn("Elasticsearch returned " + responseCode + ", retrying in " + backoff + "ms (attempt " + retries + "/" + maxRetries + ")");
                        Thread.sleep(backoff);
                    }
                } else {
                    addError("Elasticsearch returned non-retryable status " + responseCode + ", dropping " + events.size() + " events");
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                addWarn("Interrupted during bulk send, dropping " + events.size() + " events");
                return;
            } catch (IOException e) {
                retries++;
                if (retries <= maxRetries) {
                    long backoff = (long) Math.pow(2, retries) * 250;
                    addWarn("IO error sending bulk request, retrying in " + backoff + "ms: " + e.getMessage());
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    addError("Failed to send bulk request after " + maxRetries + " retries, dropping " + events.size() + " events", e);
                }
            }
        }
    }

    /**
     * Performs the HTTP POST to the Elasticsearch _bulk endpoint.
     */
    private int doHttpPost(String body) throws IOException {
        URL endpoint = new URL(bulkUrl);
        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(connectTimeoutMillis);
            conn.setReadTimeout(readTimeoutMillis);
            conn.setRequestProperty("Content-Type", "application/x-ndjson");

            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(payload.length);

            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
                out.flush();
            }

            return conn.getResponseCode();
        } finally {
            conn.disconnect();
        }
    }

    // ─── NDJSON Building ─────────────────────────────────────────────────────────

    /**
     * Builds the NDJSON body for the Elasticsearch Bulk API.
     * Each event produces two lines: the {@code {"create":{}}} action line
     * and the JSON document line encoded by the configured encoder.
     */
    private String buildNdjsonBody(List<ILoggingEvent> events) {
        StringBuilder sb = new StringBuilder(events.size() * 512);
        String actionLine = "{\"create\":{}}\n";

        for (ILoggingEvent event : events) {
            byte[] encoded = encoder.encode(event);
            // The encoder typically appends a trailing newline; strip it before re-adding
            String jsonDoc = new String(encoded, StandardCharsets.UTF_8).stripTrailing();
            sb.append(actionLine);
            sb.append(jsonDoc);
            sb.append('\n');
        }
        return sb.toString();
    }

    // ─── Getters & Setters (used by Logback's Joran configurator) ────────────────

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDataStreamType() {
        return dataStreamType;
    }

    public void setDataStreamType(String dataStreamType) {
        this.dataStreamType = dataStreamType;
    }

    public String getDataStreamDataset() {
        return dataStreamDataset;
    }

    public void setDataStreamDataset(String dataStreamDataset) {
        this.dataStreamDataset = dataStreamDataset;
    }

    public String getDataStreamNamespace() {
        return dataStreamNamespace;
    }

    public void setDataStreamNamespace(String dataStreamNamespace) {
        this.dataStreamNamespace = dataStreamNamespace;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getFlushIntervalMillis() {
        return flushIntervalMillis;
    }

    public void setFlushIntervalMillis(long flushIntervalMillis) {
        this.flushIntervalMillis = flushIntervalMillis;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}