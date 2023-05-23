package ai.zerok.javaagent.exporter.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RedisHandler {
    String redisHostName = "redis.redis.svc.cluster.local";
    int redisPort = 6379;
    int redisDB = 3;
    int ttlInSeconds = 15 * 60; // Set the TTL to 15 mins
    int batchSize = 100; // Set the desired batch size
    long durationMillis = 5000; // Set the desired duration in milliseconds

    Gson gson = new GsonBuilder().create();
    Jedis jedis;
    Pipeline pipeline;
    int count; // Counter for tracking the number of items added
    long startTime; // Start time

    public RedisHandler() {
        jedis = new Jedis(redisHostName, redisPort);
        pipeline = jedis.pipelined();
        count = 0;
        startTime = System.currentTimeMillis();
        jedis.select(redisDB);
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
        if (count >= batchSize || System.currentTimeMillis() - startTime >= durationMillis) {
            // Execute the pipeline
            pipeline.sync();

            // Reset the counter and update the start time
            count = 0;
            startTime = System.currentTimeMillis();
        }
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
