package ai.zerok.javaagent.exporter;

import ai.zerok.javaagent.exporter.internal.Endpoint;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import javax.annotation.Nullable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.*;

public final class ZKSpanUtils {

    static Endpoint getLocalEndpoint(SpanData spanData) {
        Attributes resourceAttributes = spanData.getResource().getAttributes();
        // use the service.name from the Resource, if it's been set.
        String serviceNameValue = resourceAttributes.get(ResourceAttributes.SERVICE_NAME);
        if (serviceNameValue == null) {
            serviceNameValue = Resource.getDefault().getAttribute(ResourceAttributes.SERVICE_NAME);
        }

        InetAddress ipAddressSupplier = null;
        try {
            ipAddressSupplier = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        Endpoint endpoint = new Endpoint();
        // In practice should never be null unless the default Resource spec is changed.
        if (serviceNameValue != null) {
            endpoint.setName(serviceNameValue);
        } else {
            endpoint.setName(ipAddressSupplier.getHostName());
        }

        if(ipAddressSupplier instanceof Inet6Address){
            endpoint.setIpv6(ipAddressSupplier.getHostAddress());
        }else if (ipAddressSupplier instanceof Inet4Address) {
            endpoint.setIpv4(ipAddressSupplier.getHostAddress());
        }

        return endpoint;
    }

    @Nullable
    static Endpoint getRemoteEndpoint(SpanData spanData) {
        if (spanData.getKind() == SpanKind.CLIENT || spanData.getKind() == SpanKind.PRODUCER) {
            // TODO: Implement fallback mechanism:
            // https://opentelemetry.io/docs/reference/specification/trace/sdk_exporters/zipkin/#otlp---zipkin
            Endpoint endpoint = new Endpoint();
            Attributes attributes = spanData.getAttributes();

            endpoint.setName(attributes.get(PEER_SERVICE));
            endpoint.setHost(attributes.get(NET_PEER_NAME));
            endpoint.setPort(attributes.get(NET_PEER_PORT));
            endpoint.setIpv4(attributes.get(NET_SOCK_PEER_ADDR));
            endpoint.setIpv6(attributes.get(NET_SOCK_PEER_ADDR));

            return endpoint;
        }
        return null;
    }

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
        Endpoint remoteEndpoint = ZKSpanUtils.getRemoteEndpoint(spanData);
        System.out.printf("Remote = %s\n", remoteEndpoint);
        Endpoint localEndpoint = ZKSpanUtils.getLocalEndpoint(spanData);
        System.out.printf("Local = %s\n", localEndpoint);
    }
}
