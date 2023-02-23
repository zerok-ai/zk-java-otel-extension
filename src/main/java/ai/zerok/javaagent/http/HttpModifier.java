package ai.zerok.javaagent.http;

import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;

public class HttpModifier {


    public static HttpServletResponse addTraceHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        boolean isTracestatePresent = false;
        while(headerNames.hasMoreElements()){
            String requestheaderName = headerNames.nextElement();
            if(requestheaderName.equals(Utils.getTraceStateKey())){
                isTracestatePresent = true;
            }
        }
        if(!isTracestatePresent){
            Span span = Java8BytecodeBridge.currentSpan();
            SpanContext spanContext = span.getSpanContext();
            String spanId = spanContext.getSpanId();
            String traceId = spanContext.getTraceId();
            String parentSpandId = Utils.getParentSpandId(span);
            String traceState = Utils.getTraceState(spanId);
            String traceParent = Utils.getTraceParent(traceId, spanId);
            httpServletResponse.addHeader(Utils.getTraceParentKey(), traceParent);
            httpServletResponse.addHeader(Utils.getTraceStateKey(), traceState);
            System.out.println("Adding trace headers");
        }

        return httpServletResponse;
    }

}
