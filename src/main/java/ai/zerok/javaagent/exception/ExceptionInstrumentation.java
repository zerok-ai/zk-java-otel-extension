package ai.zerok.javaagent.exception;

import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;

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

            if (responseCode != 200) {
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

}
