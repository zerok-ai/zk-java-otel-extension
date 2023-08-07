package ai.zerok.javaagent.http.spring;

import ai.zerok.javaagent.utils.CollectionUtils;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import jakarta.servlet.http.HttpServletRequest;
import ai.zerok.javaagent.logger.ZkLogger;
import jakarta.servlet.http.HttpServletResponse;

public class HttpModifier {

    private static final String log_tag = "SprintHttpModifier";
    private static boolean isHeaderPresent(String key, HttpServletRequest httpServletRequest){
        return CollectionUtils.isKeyPresentIn(httpServletRequest.getHeaderNames(), key);
    }

    private static boolean isHeaderPresent(String key, HttpServletResponse httpServletResponse){
        return CollectionUtils.isKeyPresentIn(httpServletResponse.getHeaderNames(), key);
    }

    public static HttpServletResponse addTraceHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        //TraceState
//        String currentTraceState = httpServletResponse.getHeader(Utils.getTraceStateKey());
//        ZkTraceState zkTraceState = TraceUtils.extractZkTraceState(currentTraceState);
//        String finalTraceState = "";
//        if(zkTraceState != null){
//            finalTraceState = TraceUtils.appendToTraceState(zkTraceState.getOthers(), Utils.getTraceState());
//        }else{
//            finalTraceState = Utils.getTraceState();
//        }
//        httpServletResponse.setHeader(Utils.getTraceStateKey(), finalTraceState);

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
            ZkLogger.debug(log_tag,"Adding trace headers to response "+traceParent);
        }
        return httpServletResponse;
    }

//    public static HttpServletResponse addTraceHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
//        boolean isTracestatePresent = isTraceIdentifierPresent(httpServletRequest);
//
//        if(false) {
//            System.out.println("------------------------");
//            System.out.println("Request URI - " + httpServletRequest.getMethod() + "---" + httpServletRequest.getRequestURI());
//            Span span1 = Java8BytecodeBridge.currentSpan();
//            SpanContext spanContext1 = span1.getSpanContext();
//            String spanId1 = spanContext1.getSpanId();
//            String parentSpandId1 = Utils.getParentSpandId(span1);
//            System.out.println("SpanId - " + spanId1);
//            System.out.println("ParentSpanId - " + parentSpandId1);
//            System.out.println("-----------REQUEST HEADERS-------------");
//            System.out.println("tracestate - " + httpServletRequest.getHeader("tracestate"));
//            System.out.println("traceparent - " + httpServletRequest.getHeader("traceparent"));
//            System.out.println("-----------RESPONSE HEADERS-------------");
//            System.out.println("tracestate - " + httpServletResponse.getHeader("tracestate"));
//            System.out.println("traceparent - " + httpServletResponse.getHeader("traceparent"));
//            System.out.println("------------------------");
//            System.out.println("");
//            System.out.println("");
//            System.out.println("");
//        }
//
////        isTracestatePresent = isTracestatePresent || isTraceIdentifierPresent(httpServletResponse);// httpServletResponse.getHeaderNames().contains(Utils.getTraceStateKey());
//
//        if(!isTracestatePresent){
//            Span span = Java8BytecodeBridge.currentSpan();
//            SpanContext spanContext = span.getSpanContext();
//            String spanId = spanContext.getSpanId();
//            String traceId = spanContext.getTraceId();
//            String parentSpandId = Utils.getParentSpandId(span);
//            String traceState = Utils.getTraceState(parentSpandId);
//            String traceParent = Utils.getTraceParent(traceId, spanId);
//            httpServletResponse.addHeader(Utils.getTraceParentKey(), traceParent);
//            httpServletResponse.addHeader(Utils.getTraceStateKey(), traceState);
//            System.out.println("Adding trace headers");
//        }
//
//        return httpServletResponse;
//    }

}
