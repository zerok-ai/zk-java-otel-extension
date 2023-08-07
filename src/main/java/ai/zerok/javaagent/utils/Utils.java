package ai.zerok.javaagent.utils;

import ai.zerok.javaagent.exporter.internal.RedisHandler;
import ai.zerok.javaagent.exporter.internal.SpanDetails;
import ai.zerok.javaagent.exporter.internal.TraceDetails;
import io.opentelemetry.api.trace.SpanContext;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

public final class Utils {
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
            System.out.println("Exception caught while getting parent span id.");
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
