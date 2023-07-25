package ai.zerok.javaagent.exporter;

import ai.zerok.javaagent.exporter.internal.RedisHandler;
import ai.zerok.javaagent.exporter.internal.SpanDetails;
import ai.zerok.javaagent.exporter.internal.TraceDetails;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ai.zerok.javaagent.exporter.ZKSpanUtils.printSpan;
import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.*;

public class ZKExporter implements SpanExporter {
    RedisHandler redisHandler = new RedisHandler();
    Map<String, TraceDetails> traceStore = new HashMap<>();
    boolean SET_HTTP_ENDPOINT = false;
    boolean SET_SPAN_ATTRIBUTES = false;

    @Override
    public CompletableResultCode export(Collection<SpanData> spanDataList) {
        CompletableResultCode result = new CompletableResultCode();
        try {
            // export logic for processing the span data.
            traceStore = generateStoreForSpanData(spanDataList);

            // queuing the telemetry data to sync with redis
            for (String traceId : traceStore.keySet()) {
                TraceDetails traceDetails = traceStore.get(traceId);
                redisHandler.putTraceData(traceId, traceDetails);
            }

            traceStore.clear();
        } catch (Exception e) {
            result.fail();
            return result;
        }
        result.succeed();
        return result;
    }

    private Map<String, TraceDetails> generateStoreForSpanData(Collection<SpanData> spanDataList) {
        for (SpanData spanData : spanDataList) {
            String traceId = spanData.getTraceId();
            if (!traceStore.containsKey(traceId)) {
                traceStore.put(traceId, new TraceDetails());
            }

            SpanDetails spanDetails = new SpanDetails();
            spanDetails.setParentSpanID(spanData.getParentSpanId());
            spanDetails.setSpanKind(spanData.getKind());
            spanDetails.setLocalEndpoint(ZKSpanUtils.getLocalEndpoint(spanData));
            spanDetails.setRemoteEndpoint(ZKSpanUtils.getRemoteEndpoint(spanData));

            Attributes attributes = spanData.getAttributes();
            if(SET_SPAN_ATTRIBUTES) {
                spanDetails.setAttributes(attributes.asMap().toString());
            }

            if(attributes.get(DB_SYSTEM) != null) {
                spanDetails.setProtocol(attributes.get(DB_SYSTEM));
            } else if(attributes.get(HTTP_METHOD) != null) {
                spanDetails.setProtocol(attributes.get(NET_PROTOCOL_NAME));
                if(SET_HTTP_ENDPOINT) {
                    String httpRoute = attributes.get(HTTP_ROUTE);
                    String netPeerName = attributes.get(NET_PEER_NAME);
                    String httpURL = attributes.get(HTTP_URL);
                    if (httpRoute == null || httpRoute.isEmpty()) {
                        if (netPeerName != null && !netPeerName.isEmpty() && httpURL != null && !httpURL.isEmpty()) {
                            httpRoute = httpURL.substring(httpURL.indexOf(netPeerName) + netPeerName.length());
                        } else {
                            httpRoute = "";
                        }
                    }
                    spanDetails.setEndpoint("[" + attributes.get(HTTP_METHOD) + "]" + httpRoute);
                }
            } else {
                spanDetails.setProtocol(attributes.get(NET_PROTOCOL_NAME));
            }

            TraceDetails traceDetails = traceStore.get(traceId);
            traceDetails.setSpanDetails(spanData.getSpanId(), spanDetails);
        }
        return traceStore;
    }
    @Override
    public CompletableResultCode flush() {
        // Flush method is called on application exit, manually or on a scheduled interval
        // to ensure that any buffered telemetry data is sent immediately.
        CompletableResultCode result = new CompletableResultCode();

        try {
            // queuing the telemetry data to sync with redis
            for (String traceId : traceStore.keySet()) {
                TraceDetails traceDetails = traceStore.get(traceId);
                redisHandler.putTraceData(traceId, traceDetails);
            }
            redisHandler.forceSync();
        } catch (Exception e) {
            result.fail();
            return result;
        }

        result.succeed();
        return result;
    }

    @Override
    public CompletableResultCode shutdown() {
        // Cleanup logic when the exporter is shut down
        redisHandler.shutdown();

        CompletableResultCode result = new CompletableResultCode();
        result.succeed();
        return result;
    }

    @Override
    public void close() {
        SpanExporter.super.close();
    }

    public static ZKExporterBuilder builder() {
        return new ZKExporterBuilder();
    }
}
