package ai.zerok.javaagent.utils;

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
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

public final class Utils {

    public static final String operatorUrl = "http://zerok-operator-service.zerok-operator-system.svc.cluster.local:8127/exception";
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
        try {
            String traceId = span.getSpanContext().getTraceId();
            String spanId = span.getSpanContext().getSpanId();
            URL url = new URL(operatorUrl);
            URLConnection con = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)con;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", "application/json");
            String traceParent = getTraceParent(traceId, spanId);
            httpURLConnection.setRequestProperty(traceParentKey,traceParent);
            String parentSpanId = Utils.getParentSpandId(span);
            String traceState = getTraceState(parentSpanId);
            if (traceState != null ){
                httpURLConnection.setRequestProperty(traceStateKey,traceState);
            }

            httpURLConnection.setDoOutput(true);

            String body = getExceptionPayload(throwable);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code " + responseCode);
            return responseCode;

        }
        catch (Throwable e) {
            System.out.println("Exception caught while sending exception data to operator."+e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return 500;
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

}
