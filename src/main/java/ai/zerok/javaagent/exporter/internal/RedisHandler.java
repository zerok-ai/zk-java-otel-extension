package ai.zerok.javaagent.exporter.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import redis.clients.jedis.Jedis;
import java.util.HashMap;
public class RedisHandler {
    String redisHostName = "redis.redis.svc.cluster.local";
    int redisPort = 6379;
    int redisDB = 3;
    int ttlInSeconds = 15 * 60; // Set the TTL to 15 mins

    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    Jedis jedis;

    public RedisHandler() {
        jedis = new Jedis(redisHostName, redisPort);
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
        jedis.hmset(traceId, spanJsonMap);
        jedis.expire(traceId, ttlInSeconds);
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
