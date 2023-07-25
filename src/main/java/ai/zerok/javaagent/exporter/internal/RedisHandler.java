package ai.zerok.javaagent.exporter.internal;

import ai.zerok.javaagent.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class RedisHandler {
    private static final Logger LOGGER = Utils.getLogger(RedisHandler.class);
//    String redisHostName = "localhost";
//    int redisPort = 6371;
    String redisHostName = "redis-master.zk-client.svc.cluster.local";
    int redisPort = 6379;
    int redisDB = 3;
    int ttlInSeconds = 15 * 60; // Set the TTL to 15 mins
    int batchSize = 100; // Set the desired batch size
    long durationMillis = 5 * 1000; // Set the desired duration for sync (In ms)
    long timerSyncDuration = 30 * 1000; // Sync items in pipeline after this duration is elapsed.n (In ms)

    Gson gson = new GsonBuilder().create();
    Jedis jedis;
    Pipeline pipeline;
    int count; // Counter for tracking the number of items added
    long startTime; // Start time
    Timer timer = new Timer();

    public RedisHandler() {
        initializeRedisConn();
    }

    private void initializeRedisConn() {
        jedis = new Jedis(redisHostName, redisPort);
        pipeline = jedis.pipelined();
        count = 0;
        startTime = System.currentTimeMillis();
        jedis.select(redisDB);
        timer.schedule(syncTask, 0, timerSyncDuration);
    }

    TimerTask syncTask = new TimerTask() {
        @Override
        public void run() {
            // This happens only if the data volume does not exceed the batchsize or a long duration has elapsed.
            LOGGER.config("Pipeline synchronized on timer");
            syncPipeline();
        }
    };

    private void syncPipeline() {
        try {
            if (count >= batchSize || System.currentTimeMillis() - startTime >= durationMillis) {
                // Execute the pipeline
                pipeline.sync();
                LOGGER.config("Pipeline synchronized on batchsize/duration");

                // Reset the counter and update the start time
                count = 0;
                startTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            LOGGER.severe("Exception occurred while syncing pipeline." + e.getMessage());
            e.printStackTrace();
        }
    }

    public void putTraceData(String traceId, TraceDetails traceDetails) {
        // Reconnect redis if connection is broken
        if (!jedis.isConnected() || jedis.isBroken()) {
            LOGGER.config("Reconnecting Redis");
            jedis.disconnect();
            initializeRedisConn();
        }

        LOGGER.config(gson.toJson(traceDetails));
        Map<String, SpanDetails> spansHashMap = traceDetails.getSpanDetailsMap();
        Map<String, String> spanJsonMap = new HashMap<>();

        for(String spanId: spansHashMap.keySet()) {
            SpanDetails spanDetails = spansHashMap.get(spanId);
            spanJsonMap.put(spanId, gson.toJson(spanDetails));
        }
        // Set updated spans in hashmap for trace ID.
        pipeline.hmset(traceId, spanJsonMap);
        pipeline.expire(traceId, ttlInSeconds);

        // Sync to redis server if batch size has exceeded or sync duration has elapsed.
        syncPipeline();
    }


    public void forceSync() {
        pipeline.sync();
    }

    public void shutdown() {
        pipeline.sync();
        jedis.disconnect();
    }

    public TraceDetails getTraceData(String traceId) {
        Map<String, String> spanJsonMap = jedis.hgetAll(traceId);
        Map<String, SpanDetails> spansHashMap = new HashMap<>();

        for(String spanId: spanJsonMap.keySet()) {
            spansHashMap.put(spanId, gson.fromJson(spanJsonMap.get(spanId), SpanDetails.class));
        }

        TraceDetails traceDetails = new TraceDetails();
        traceDetails.setSpanDetailsMap(spansHashMap);
        return traceDetails;
    }

}
