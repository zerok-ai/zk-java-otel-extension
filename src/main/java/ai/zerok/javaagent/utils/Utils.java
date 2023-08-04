package ai.zerok.javaagent.utils;

import ai.zerok.javaagent.logger.LogsConfig;
import ai.zerok.javaagent.logger.ZkLogger;
import io.opentelemetry.api.trace.SpanContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import io.opentelemetry.api.trace.Span;

public final class Utils {

    private static Boolean isInitialized = false;
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

    public static String getExceptionPayload(Throwable r) {
        HashMap<String,String> map = new HashMap<>();
        map.put("message",r.getMessage());
        String stackTraceString = Arrays.toString(r.getStackTrace());
        map.put("stacktrace",stackTraceString);
        System.out.println("json string is "+map);
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
            System.out.println("Exception caught while getting parent span id.");
        }
        return parentSpanId;
    }
}
