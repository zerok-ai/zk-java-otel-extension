package ai.zerok.javaagent.exception;

import ai.zerok.javaagent.logger.ZkLogger;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class ExceptionInstrumentation {

    private static final String log_tag = "ExceptionInstrumentation";
    public static final String operatorUrl = "http://zk-operator.zk-client.svc.cluster.local/exception";
    public static int sendExceptionDataToOperator(Throwable throwable, Span span) {
        ZkLogger.debug(log_tag,"In sendExceptionDataToOperator");
        try {
            String traceId = span.getSpanContext().getTraceId();
            String parentSpanId = span.getSpanContext().getSpanId();

            ZkLogger.debug(log_tag,"Preparing to send Exception for trace ID:"+traceId+"& SpanID:"+parentSpanId+".");

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
            ZkLogger.debug(log_tag,"Response Code " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                ZkLogger.error(log_tag,"Failed to upload exception data. Got " + responseCode );
                return responseCode;
            }

            /* Upload data to redis. */
            String traceParent = httpURLConnection.getRequestProperty(Utils.getTraceParentKey());
            ZkLogger.debug(log_tag,"traceparent : " + traceParent);
            if(traceParent == null || traceParent.isEmpty()) {
                ZkLogger.error(log_tag,"missing traceparent " + traceParent);
                return responseCode;
            }

            return responseCode;
        }
        catch (Throwable e) {
            ZkLogger.error(log_tag,"Exception caught while sending exception data to operator."+e.getMessage());
            e.getStackTrace();
        }
        return 500;
    }


}
