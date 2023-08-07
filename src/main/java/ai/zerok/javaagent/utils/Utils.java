package ai.zerok.javaagent.utils;

import ai.zerok.javaagent.logger.ZkLogger;
import io.opentelemetry.api.trace.SpanContext;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import io.opentelemetry.api.trace.Span;

public final class Utils {

    private static final String log_tag = "Utils";

    private static final String traceParentKey = "traceparent";
    private static final String traceStateKey = "tracestate";

    private static final String traceStateZkKey = "zerok";
    private static final String traceStatePrefix = traceStateZkKey + "=";

    public static String getTraceParentKey() {
         return traceParentKey;
    }

    public static String getTraceStateKey() {
        return traceStateKey;
    }

    public static String getTraceStateZkKey() {
        return traceStateZkKey;
    }

    public static Timer timer = setupTimer();

    public static String getExceptionPayload(Throwable r) {
        HashMap<String,String> map = new HashMap<>();
        map.put("message",r.getMessage());
        String stackTraceString = Arrays.toString(r.getStackTrace());
        map.put("stacktrace",stackTraceString);
        ZkLogger.debug(log_tag ,"json string is "+map);
        return map.toString();
    }

    public static String getTraceParent(String traceId, String spanId) {
        String sep = "-";
        return "00" + sep + traceId + sep + spanId + sep + "00";
    }

    public static String getTraceState(String spandId) {
        return traceStatePrefix + spandId;
    }

    public static String getParentSpandId(Span span) {
        String parentSpanId = null;
        try {
            Class<?> classObj = span.getClass();
            Method getParent = classObj.getDeclaredMethod("getParentSpanContext");
            getParent.setAccessible(true);
            SpanContext parentSpan = (SpanContext) getParent.invoke(span);
            parentSpanId = parentSpan.getSpanId();
        }
        catch (Exception e) {
            ZkLogger.error(log_tag ,"Exception caught while getting parent span id.");
        }
        return parentSpanId;
    }

    public static String getCurrentTime() {
        long epochTime = System.currentTimeMillis() / 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Date date = new Date(epochTime * 1000);
        return sdf.format(date);
    }

    public static Timer setupTimer() {
        Timer timer = new Timer();

        // Create a TimerTask
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ZkLogger.debug(log_tag, "Timer tick.");
                updateConfigFromEnvVariables();
            }
        };

        // Schedule the TimerTask to run every 1 minute
        timer.schedule(task, 0, 60000);

        return timer;
    }

    public static void updateConfigFromEnvVariables() {
        String envVarName = "ZK_LOG_LEVEL";
        String value = readEnvironmentVariable(envVarName);
        ZkLogger.setLogLevel(value);
    }

    public static String readEnvironmentVariable(String variableName) {
        Map<String, String> env = System.getenv();
        return env.get(variableName);
    }

}
