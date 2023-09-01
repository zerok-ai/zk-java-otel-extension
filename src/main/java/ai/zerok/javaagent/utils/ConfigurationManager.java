package ai.zerok.javaagent.utils;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    String redisHostName = "redis.zk-client.svc.cluster.local";
    int redisPort = 6379;
    int redisDB = 3;
    int ttlInSeconds = 15 * 60; // Set the TTL to 15 mins
    int batchSize = 100; // Set the desired batch size
    long durationMillis = 5 * 1000; // Set the desired duration for sync (In ms)
    long timerSyncDuration = 30 * 1000; // Sync items in pipeline after this duration is elapsed.n (In ms)
    private Map<String, Object> fallbackValues;

    public ConfigurationManager() {
        fallbackValues = new HashMap<>();
        // Set fallback values of different types
        setFallbackValue("ZK_REDIS_HOSTNAME", redisHostName);
        setFallbackValue("ZK_REDIS_PORT", redisPort);
        setFallbackValue("ZK_REDIS_DB", redisDB);
        setFallbackValue("ZK_REDIS_TTL", ttlInSeconds);
        setFallbackValue("ZK_REDIS_BATCH_SIZE", batchSize);
        setFallbackValue("ZK_REDIS_DURATION_MILLIS", durationMillis);
        setFallbackValue("ZK_REDIS_TIMER_SYNC_DURATION", timerSyncDuration);
    }

    public ConfigurationManager(Map<String, Object> fallbackValues) {
        this.fallbackValues = fallbackValues;
    }

    public void setFallbackValue(String variableName, Object fallbackValue) {
        fallbackValues.put(variableName, fallbackValue);
    }

    public String getStringValue(String variableName) {
        String value = System.getenv(variableName);
        if (value == null && fallbackValues.containsKey(variableName)) {
            Object fallbackValue = fallbackValues.get(variableName);
            if (fallbackValue instanceof String) {
                return (String) fallbackValue;
            }
        }
        return value;
    }

    public long getLongValue(String variableName) {
        String value = getStringValue(variableName);
        try {
            return (value != null) ? Long.parseLong(value) : 0; // Default value of 0 for long
        } catch (NumberFormatException e) {
            return 0; // Default value of 0 for long
        }
    }

    public int getIntValue(String variableName) {
        String value = getStringValue(variableName);
        try {
            return (value != null) ? Integer.parseInt(value) : 0; // Default value of 0 for int
        } catch (NumberFormatException e) {
            return 0; // Default value of 0 for int
        }
    }
}
