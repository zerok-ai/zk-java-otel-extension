package ai.zerok.javaagent.exception;

import ai.zerok.javaagent.exporter.internal.Endpoint;
import ai.zerok.javaagent.exporter.internal.RedisHandler;
import ai.zerok.javaagent.exporter.internal.SpanDetails;
import ai.zerok.javaagent.exporter.internal.TraceDetails;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ExceptionInstrumentation {
    private static final Logger LOGGER = Utils.getLogger(ExceptionInstrumentation.class);
    public static final String operatorUrl = "http://zk-operator.zk-client.svc.cluster.local/exception";
    public static int sendExceptionDataToOperator(Throwable throwable, Span span) {
        LOGGER.config("In sendExceptionDataToOperator");
        try {
            Scope scope = span.makeCurrent();
            String traceId = span.getSpanContext().getTraceId();
            String parentSpanId = span.getSpanContext().getSpanId();

            LOGGER.config("Preparing to send Exception for trace ID:"+traceId+"& SpanID:"+parentSpanId+".");

            URL url = new URL(operatorUrl);
            URLConnection con = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)con;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", "application/json");

            httpURLConnection.setDoOutput(true);

            String body = Utils.getExceptionPayload(throwable);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            int responseCode = httpURLConnection.getResponseCode();
            LOGGER.config("Response Code " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.severe("Failed to upload exception data. Got " + responseCode );
                return responseCode;
            }
            scope.close();
            /* Upload data to redis. */
            String traceParent = httpURLConnection.getRequestProperty(Utils.getTraceParentKey());
            LOGGER.config("traceparent : " + traceParent);
            if(traceParent == null || traceParent.isEmpty()) {
                LOGGER.config("missing traceparent " + traceParent);
                return responseCode;
            }

            updateRedisWithExceptionSpan(traceId, parentSpanId, traceParent);

            return responseCode;
        }
        catch (Throwable e) {
            LOGGER.severe("Exception caught while sending exception data to operator."+e.getMessage());
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

}
