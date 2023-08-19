package ai.zerok.javaagent.exception;

import ai.zerok.javaagent.exporter.internal.Endpoint;
import ai.zerok.javaagent.exporter.internal.RedisHandler;
import ai.zerok.javaagent.exporter.internal.SpanDetails;
import ai.zerok.javaagent.exporter.internal.TraceDetails;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
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

            OkHttpClient client = new OkHttpClient();
            String body = Utils.getExceptionPayload(throwable);
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(mediaType, body);

            Request request = new Request.Builder()
                    .url(operatorUrl)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
            int responseCode = response.code();
            System.out.println("Response Code " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Failed to upload exception data. Got " + responseCode );
                return responseCode;
            }
            String traceParent = request.header(Utils.getTraceParentKey());
            System.out.println("traceparent : " + traceParent);
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
