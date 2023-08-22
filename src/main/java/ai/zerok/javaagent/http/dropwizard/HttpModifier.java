package ai.zerok.javaagent.http.dropwizard;

import ai.zerok.javaagent.utils.CollectionUtils;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpModifier {

    private static boolean isHeaderPresent(String key, HttpServletRequest httpServletRequest){
        return CollectionUtils.isKeyPresentIn(httpServletRequest.getHeaderNames(), key);
    }

    private static boolean isHeaderPresent(String key, HttpServletResponse httpServletResponse){
        return CollectionUtils.isKeyPresentIn(httpServletResponse.getHeaderNames(), key);
    }

    public static HttpServletResponse addTraceHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        //TraceParent
        boolean isTraceStatePresent = isHeaderPresent(Utils.getTraceParentKey(), httpServletRequest);
        if(!isTraceStatePresent){
            Span span = Java8BytecodeBridge.currentSpan();
            SpanContext spanContext = span.getSpanContext();
            String spanId = spanContext.getSpanId();
            String traceId = spanContext.getTraceId();
            String parentSpandId = Utils.getParentSpandId(span);
            String traceParent = Utils.getTraceParent(traceId, parentSpandId);
            httpServletResponse.setHeader(Utils.getTraceParentKey(), traceParent);
            System.out.println("Adding trace headers to response");
        }
        return httpServletResponse;
    }

}
