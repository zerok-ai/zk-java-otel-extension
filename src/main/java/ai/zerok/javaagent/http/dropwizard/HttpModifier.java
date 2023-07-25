package ai.zerok.javaagent.http.dropwizard;

import ai.zerok.javaagent.utils.CollectionUtils;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
//
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
            LOGGER.fine("Adding trace headers to response");
        }
        return httpServletResponse;
    }

//    public static HttpServletResponse addTraceHeaders(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
//        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
//        boolean isTracestatePresent = false;
//        while(headerNames.hasMoreElements()){
//            String requestheaderName = headerNames.nextElement();
//            if(requestheaderName.equals(Utils.getTraceStateKey())){
//                isTracestatePresent = true;
//            }
//        }
//
//        isTracestatePresent = isTracestatePresent || httpServletResponse.getHeaderNames().contains(Utils.getTraceStateKey());
////        if(!isTracestatePresent) {
////            Collection<String> responseHeaderNames = httpServletResponse.getHeaderNames();
////            isTracestatePresent = responseHeaderNames.contains(Utils.getTraceStateKey());
////        }
//
//        if(!isTracestatePresent){
//            Span span = Java8BytecodeBridge.currentSpan();
//            SpanContext spanContext = span.getSpanContext();
//            String spanId = spanContext.getSpanId();
//            String traceId = spanContext.getTraceId();
//            String parentSpandId = Utils.getParentSpandId(span);
//            String traceState = Utils.getTraceState(spanId);
//            String traceParent = Utils.getTraceParent(traceId, spanId);
//            httpServletResponse.addHeader(Utils.getTraceParentKey(), traceParent);
//            httpServletResponse.addHeader(Utils.getTraceStateKey(), traceState);
//            LOGGER.fine("Adding trace headers");
//        }
//
//        return httpServletResponse;
//    }

}
