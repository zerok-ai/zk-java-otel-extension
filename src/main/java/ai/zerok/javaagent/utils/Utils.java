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

    public static final String operatorUrl = "http://zk-operator.zk-client.svc.cluster.local/exception";
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

    public static int sendExceptionDataToOperator(Throwable throwable, Span span) {
        System.out.println("In sendExceptionDataToOperator");
        try {
            String traceId = span.getSpanContext().getTraceId();
            String parentSpanId = span.getSpanContext().getSpanId();

            System.out.println("Preparing to send Exception for trace ID:"+traceId+"& SpanID:"+parentSpanId+".");

            URL url = new URL(operatorUrl);
            URLConnection con = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)con;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", "application/json");

            httpURLConnection.setDoOutput(true);

            String body = getExceptionPayload(throwable);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Failed to upload exception data. Got " + responseCode );
                return responseCode;
            }

            /* Upload data to redis. */
            String traceParent = httpURLConnection.getRequestProperty(Utils.getTraceParentKey());
            System.out.println("traceparent : " + traceParent);
            if(traceParent == null || traceParent.isEmpty()) {
                System.out.println("missing traceparent " + traceParent);
                return responseCode;
            }

            Utils.updateRedisWithExceptionSpan(traceId, parentSpanId, traceParent);

            return responseCode;
        }
        catch (Throwable e) {
            System.out.println("Exception caught while sending exception data to operator."+e.getMessage());
            e.getStackTrace();
        }
        return 500;
    }

    private static void updateRedisWithExceptionSpan(String traceId, String parentSpanId, String exceptionTraceParent) {
        String spanId = Utils.extractSpanId(exceptionTraceParent);
        SpanDetails exceptionSpanDetails = new SpanDetails();
        exceptionSpanDetails.setSpanKind(SpanKind.CLIENT);
        exceptionSpanDetails.setParentSpanID(parentSpanId);
        exceptionSpanDetails.setProtocol("exception");

        TraceDetails exceptionTraceDetails = new TraceDetails();
        exceptionTraceDetails.setSpanDetails(spanId, exceptionSpanDetails);

        RedisHandler redisHandler = new RedisHandler();
        redisHandler.putTraceData(traceId, exceptionTraceDetails);
        redisHandler.forceSync();
    }

    private static String getExceptionPayload(Throwable r) {
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

    private static String extractTraceId(String traceparentHeader) {
        String[] parts = traceparentHeader.split("-");
        if (parts.length >= 2) {
            String traceId = parts[1];
            if (isValidHexadecimal(traceId)) {
                return traceId;
            }
        }
        return null;
    }

    private static String extractSpanId(String traceparentHeader) {
        String[] parts = traceparentHeader.split("-");
        if (parts.length >= 3) {
            String spanId = parts[2];
            if (isValidHexadecimal(spanId)) {
                return spanId;
            }
        }
        return null;
    }

    private static boolean isValidHexadecimal(String value) {
        try {
            Long.parseLong(value, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
