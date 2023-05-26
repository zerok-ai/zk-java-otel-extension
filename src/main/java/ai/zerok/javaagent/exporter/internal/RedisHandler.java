package ai.zerok.javaagent.exporter.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RedisHandler {
    String redisHostName = "redis.redis.svc.cluster.local";
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
            System.out.println("Pipeline synchronized on timer");
            syncPipeline();
        }
    };

    private void syncPipeline() {
        if (count >= batchSize || System.currentTimeMillis() - startTime >= durationMillis) {
            // Execute the pipeline
            pipeline.sync();
            System.out.println("Pipeline synchronized on batchsize/duration");

            // Reset the counter and update the start time
            count = 0;
            startTime = System.currentTimeMillis();
        }
    }

    public void putTraceData(String traceId, TraceDetails traceDetails) {
        System.out.println(gson.toJson(traceDetails));
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
        jedis.shutdown();
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
