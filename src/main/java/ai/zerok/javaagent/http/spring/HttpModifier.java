package ai.zerok.javaagent.http.spring;

import ai.zerok.javaagent.utils.CollectionUtils;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.logging.Logger;

public class HttpModifier {
    private static final Logger LOGGER = Utils.getLogger(HttpModifier.class);

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
            LOGGER.config("Adding trace headers to response");
        }
        return httpServletResponse;
    }

//    public static HttpServletResponse addTraceHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
//        boolean isTracestatePresent = isTraceIdentifierPresent(httpServletRequest);
//
//        if(false) {
//            LOGGER.config("------------------------");
//            LOGGER.config("Request URI - " + httpServletRequest.getMethod() + "---" + httpServletRequest.getRequestURI());
//            Span span1 = Java8BytecodeBridge.currentSpan();
//            SpanContext spanContext1 = span1.getSpanContext();
//            String spanId1 = spanContext1.getSpanId();
//            String parentSpandId1 = Utils.getParentSpandId(span1);
//            LOGGER.config("SpanId - " + spanId1);
//            LOGGER.config("ParentSpanId - " + parentSpandId1);
//            LOGGER.config("-----------REQUEST HEADERS-------------");
//            LOGGER.config("tracestate - " + httpServletRequest.getHeader("tracestate"));
//            LOGGER.config("traceparent - " + httpServletRequest.getHeader("traceparent"));
//            LOGGER.config("-----------RESPONSE HEADERS-------------");
//            LOGGER.config("tracestate - " + httpServletResponse.getHeader("tracestate"));
//            LOGGER.config("traceparent - " + httpServletResponse.getHeader("traceparent"));
//            LOGGER.config("------------------------");
//            LOGGER.config("");
//            LOGGER.config("");
//            LOGGER.config("");
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
//            LOGGER.config("Adding trace headers");
//        }
//
//        return httpServletResponse;
//    }

}
