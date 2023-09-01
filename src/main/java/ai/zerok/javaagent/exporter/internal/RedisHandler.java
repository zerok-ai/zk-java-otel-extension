package ai.zerok.javaagent.exporter.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Pipeline;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ai.zerok.javaagent.utils.ConfigurationManager;

public class RedisHandler {
    ConfigurationManager configManager = new ConfigurationManager();

    String redisHostName = configManager.getStringValue("ZK_REDIS_HOSTNAME");
    int redisPort = configManager.getIntValue("ZK_REDIS_PORT");
    String redisPassword = configManager.getStringValue("ZK_REDIS_PASSWORD");
    int redisDB = configManager.getIntValue("ZK_REDIS_DB");
    int ttlInSeconds = configManager.getIntValue("ZK_REDIS_TTL");
    int batchSize = configManager.getIntValue("ZK_REDIS_BATCH_SIZE");
    long durationMillis = configManager.getLongValue("ZK_REDIS_DURATION_MILLIS");
    long timerSyncDuration = configManager.getLongValue("ZK_REDIS_TIMER_SYNC_DURATION");

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
        DefaultJedisClientConfig defaultJedisClientConfig = DefaultJedisClientConfig.builder().password(redisPassword).build();
        jedis = new Jedis(redisHostName, redisPort, defaultJedisClientConfig);
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
        try {
            if (count >= batchSize || System.currentTimeMillis() - startTime >= durationMillis) {
                // Execute the pipeline
                pipeline.sync();
                System.out.println("Pipeline synchronized on batchsize/duration");

                // Reset the counter and update the start time
                count = 0;
                startTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while syncing pipeline." + e.getMessage());
            e.printStackTrace();
        }
    }

    public void putTraceData(String traceId, TraceDetails traceDetails) {
        // Reconnect redis if connection is broken
        if (!jedis.isConnected() || jedis.isBroken()) {
            System.out.println("Reconnecting Redis");
            jedis.disconnect();
            initializeRedisConn();
        }

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
