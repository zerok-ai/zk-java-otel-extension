package ai.zerok.javaagent.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Utils {
    private static final Logger LOGGER = getLogger(Utils.class);
    private static Level LOG_LEVEL = Level.CONFIG;
    public static final String traceParentKey = "traceparent";
    public static final String traceStateKey = "tracestate";

    private static final String traceStateZkKey = "zerok";
    private static final String traceStatePrefix = traceStateZkKey + "=";

    private static final Map<String, Logger> classToLogerMap = new HashMap<>();

    public static Logger getLogger(Object obj){
        if(obj == null){
            return getLogger(Utils.class);
        }
        return getLogger(obj.getClass());
    }

    public static Logger getLogger(Class cls){
        return getLogger(cls, LOG_LEVEL);
    }

    public static Logger getLogger(Class cls, Level logLevel){
        String className = cls.getName();
        if(!classToLogerMap.containsKey(className)){
            Logger logger = Logger.getLogger(className);
            logger.setLevel(logLevel);
            classToLogerMap.put(className, logger);
        }
        return classToLogerMap.get(className);
    }

//    public static void setLogLevel(Level level){
//        LOG_LEVEL = level;
//    }

    public static String getTraceParentKey() {
         return traceParentKey;
    }

    public static String getTraceStateKey() {
        return traceStateKey;
    }

    public static String getTraceStatePrefix() {
        return traceStatePrefix;
    }

    public static String getTraceStateZkKey() {
        return traceStateZkKey;
    }

    public static String getExceptionPayload(Throwable r) {
        HashMap<String,String> map = new HashMap<>();
        map.put("message",r.getMessage());
        String stackTraceString = Arrays.toString(r.getStackTrace());
        map.put("stacktrace",stackTraceString);
        LOGGER.config("json string is "+map);
        return map.toString();
    }

    public static String getTraceParent(String traceId, String spanId) {
        String sep = "-";
        return "00" + sep + traceId + sep + spanId + sep + "00";
    }

    public static String getTraceState(String spandId) {
        return traceStatePrefix + spandId;
    }

    public static String getTraceState() {
        Span span = Java8BytecodeBridge.currentSpan();
        SpanContext spanContext = span.getSpanContext();
        String spanId = spanContext.getSpanId();
        return traceStatePrefix + spanId;
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
            LOGGER.severe("Exception caught while getting parent span id.");
        }
        return parentSpanId;
    }

    public static String extractTraceId(String traceparentHeader) {
        String[] parts = traceparentHeader.split("-");
        if (parts.length >= 2) {
            String traceId = parts[1];
            return traceId;
        }
        return null;
    }

    public static String extractSpanId(String traceparentHeader) {
        String[] parts = traceparentHeader.split("-");
        if (parts.length >= 3) {
            String spanId = parts[2];
            return spanId;
        }
        return null;
    }
}
