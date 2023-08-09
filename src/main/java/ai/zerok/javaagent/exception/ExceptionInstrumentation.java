package ai.zerok.javaagent.exception;

import ai.zerok.javaagent.exporter.internal.Endpoint;
import ai.zerok.javaagent.exporter.internal.RedisHandler;
import ai.zerok.javaagent.exporter.internal.SpanDetails;
import ai.zerok.javaagent.exporter.internal.TraceDetails;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ExceptionInstrumentation {
    public static final String operatorUrl = "http://zk-operator.zk-client.svc.cluster.local/exception";
    public static int sendExceptionDataToOperator(Throwable throwable, Span span) {
        System.out.println("In sendExceptionDataToOperator");
        try {
            String traceId = span.getSpanContext().getTraceId();
            String parentSpanId = span.getSpanContext().getSpanId();

            System.out.println("Preparing to send Exception for trace ID:"+traceId+"& SpanID:"+parentSpanId+".");

            HttpClient client = HttpClient.newHttpClient();
            String body = Utils.getExceptionPayload(throwable);

            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .uri(URI.create(operatorUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int responseCode = response.statusCode();
            System.out.println("Response Code " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Failed to upload exception data. Got " + responseCode );
                return responseCode;
            }

            /* Upload data to redis. */
            String traceParent = getHeader(request, Utils.getTraceParentKey());
            System.out.println("traceparent : " + traceParent);
            if(traceParent == null || traceParent.isEmpty()) {
                System.out.println("missing traceparent " + traceParent);
                return responseCode;
            }

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
        exceptionSpanDetails.setLocalEndpoint(new Endpoint());
        exceptionSpanDetails.setRemoteEndpoint(new Endpoint());

        TraceDetails exceptionTraceDetails = new TraceDetails();
        exceptionTraceDetails.setSpanDetails(spanId, exceptionSpanDetails);

        RedisHandler redisHandler = new RedisHandler();
        redisHandler.putTraceData(traceId, exceptionTraceDetails);
        redisHandler.forceSync();
    }

    private static String getHeader(HttpRequest request, String headerName) {
        Map<String, List<String>> headers = request.headers().map();
        List<String> headerValues = headers.get(headerName);
        if (headerValues != null && !headerValues.isEmpty()) {
            String headerValue = headerValues.get(0);
            return headerValue;
        } else {
            System.out.println("Header not found");
        }
        return null;
    }

}
