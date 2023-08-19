package ai.zerok.javaagent.exporter;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Map;


public final class ZKSpanUtils {

    static void printSpan(SpanData spanData) {
        System.out.printf("spanId = '%s'\n", spanData.getSpanId());
        System.out.printf("traceId = '%s'\n", spanData.getTraceId());
        System.out.printf("parentSpanId = '%s'\n", spanData.getParentSpanId());
        System.out.printf("spanKind = '%s'\n", String.valueOf(spanData.getKind()));
        System.out.printf("spanAttributes = '%d'\n", spanData.getTotalAttributeCount());
        System.out.println("Attributes = ");
        Map<AttributeKey<?>, Object> attributesMap = spanData.getAttributes().asMap();
        for(AttributeKey key: attributesMap.keySet()) {
            Object value = attributesMap.get(key);
            System.out.printf("\t%s = '%s'\n", key, value.toString());
        }
    }
}
