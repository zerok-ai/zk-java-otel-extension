package ai.zerok.javaagent.http.spring;

import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.Enumeration;

public class HttpModifier {

    private static boolean isTraceIdentifierPresent(HttpServletRequest httpServletRequest){
        Enumeration<String> requestHeaderNames = httpServletRequest.getHeaderNames();
        boolean isTraceIdentifierPresent = false;
        while(requestHeaderNames.hasMoreElements()){
            String requestheaderName = requestHeaderNames.nextElement();
            if(requestheaderName.equals(Utils.getTraceParentKey())){
                isTraceIdentifierPresent = true;
            }
        }

        return isTraceIdentifierPresent;
    }

    private static boolean isTraceIdentifierPresent(HttpServletResponse httpServletResponse){
        return httpServletResponse.getHeaderNames().contains(Utils.getTraceParentKey());
    }

    public static HttpServletResponse addTraceHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        boolean isTracestatePresent = isTraceIdentifierPresent(httpServletRequest);

        if(false) {
            System.out.println("------------------------");
            System.out.println("Request URI - " + httpServletRequest.getMethod() + "---" + httpServletRequest.getRequestURI());
            Span span1 = Java8BytecodeBridge.currentSpan();
            SpanContext spanContext1 = span1.getSpanContext();
            String spanId1 = spanContext1.getSpanId();
            String parentSpandId1 = Utils.getParentSpandId(span1);
            System.out.println("SpanId - " + spanId1);
            System.out.println("ParentSpanId - " + parentSpandId1);
            System.out.println("-----------REQUEST HEADERS-------------");
            System.out.println("tracestate - " + httpServletRequest.getHeader("tracestate"));
            System.out.println("traceparent - " + httpServletRequest.getHeader("traceparent"));
            System.out.println("-----------RESPONSE HEADERS-------------");
            System.out.println("tracestate - " + httpServletResponse.getHeader("tracestate"));
            System.out.println("traceparent - " + httpServletResponse.getHeader("traceparent"));
            System.out.println("------------------------");
            System.out.println("");
            System.out.println("");
            System.out.println("");
        }

//        isTracestatePresent = isTracestatePresent || isTraceIdentifierPresent(httpServletResponse);// httpServletResponse.getHeaderNames().contains(Utils.getTraceStateKey());

        if(!isTracestatePresent){
            Span span = Java8BytecodeBridge.currentSpan();
            SpanContext spanContext = span.getSpanContext();
            String spanId = spanContext.getSpanId();
            String traceId = spanContext.getTraceId();
            String parentSpandId = Utils.getParentSpandId(span);
            String traceState = Utils.getTraceState(parentSpandId);
            String traceParent = Utils.getTraceParent(traceId, spanId);
            httpServletResponse.addHeader(Utils.getTraceParentKey(), traceParent);
            httpServletResponse.addHeader(Utils.getTraceStateKey(), traceState);
            System.out.println("Adding trace headers");
        }

        return httpServletResponse;
    }

}
