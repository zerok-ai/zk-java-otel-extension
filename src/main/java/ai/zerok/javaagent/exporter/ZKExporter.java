package ai.zerok.javaagent.exporter;

import ai.zerok.javaagent.exporter.internal.RedisHandler;
import ai.zerok.javaagent.exporter.internal.SpanDetails;
import ai.zerok.javaagent.exporter.internal.TraceDetails;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ai.zerok.javaagent.exporter.ZKSpanUtils.printSpan;

public class ZKExporter implements SpanExporter {

    RedisHandler redisHandler = new RedisHandler();
    Map<String, TraceDetails> traceStore = new HashMap<>();

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
        } catch (Exception e) {
            result.fail();
            return result;
        }
        result.succeed();
        return result;
    }

    private Map<String, TraceDetails> generateStoreForSpanData(Collection<SpanData> spanDataList) {
        for (SpanData spanData : spanDataList) {
            printSpan(spanData);
            String traceId = spanData.getTraceId();
            if (!traceStore.containsKey(traceId)) {
                traceStore.put(traceId, new TraceDetails());
            }

            SpanDetails spanDetails = new SpanDetails();
            spanDetails.setParentSpanID(spanData.getParentSpanId());
            spanDetails.setSpanKind(spanData.getKind());
            spanDetails.setLocalEndpoint(ZKSpanUtils.getLocalEndpoint(spanData));
            spanDetails.setRemoteEndpoint(ZKSpanUtils.getRemoteEndpoint(spanData));

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
        System.out.println("ZeroK Exporter Builder");
        return new ZKExporterBuilder();
    }
}
