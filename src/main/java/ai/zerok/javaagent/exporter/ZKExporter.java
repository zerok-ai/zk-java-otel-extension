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

    @Override
    public CompletableResultCode export(Collection<SpanData> spanDataList) {
        CompletableResultCode result = new CompletableResultCode();
        try {
            // export logic for processing the span data.
            Map<String, TraceDetails> Store = generateStoreForSpanData(spanDataList);

            // queuing the telemetry data to sync with redis
            for (String traceId : Store.keySet()) {
                TraceDetails traceDetails = Store.get(traceId);
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
        Map<String, TraceDetails> Store = new HashMap<>();

        for (SpanData spanData : spanDataList) {
            printSpan(spanData);
            String traceId = spanData.getTraceId();
            if (!Store.containsKey(traceId)) {
                Store.put(traceId, new TraceDetails());
            }

            SpanDetails spanDetails = new SpanDetails();
            spanDetails.setParentSpanID(spanData.getParentSpanId());
            spanDetails.setSpanKind(spanData.getKind());
            spanDetails.setLocalEndpoint(ZKSpanUtils.getLocalEndpoint(spanData));
            spanDetails.setRemoteEndpoint(ZKSpanUtils.getRemoteEndpoint(spanData));

            TraceDetails traceDetails = Store.get(traceId);
            traceDetails.setSpanDetails(spanData.getSpanId(), spanDetails);
        }
        return Store;
    }
    @Override
    public CompletableResultCode flush() {
        // Implement flushing logic if required by your exporter
        // This method is called when a flush is triggered
        // Return the appropriate ResultCode indicating success or failure

        CompletableResultCode result = new CompletableResultCode();
        result.succeed();
        return result;
    }

    @Override
    public CompletableResultCode shutdown() {
        // Implement any cleanup logic when the exporter is shut down
        // Close connections, release resources, etc.

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
