package ai.zerok.javaagent.exporter;

import ai.zerok.javaagent.exporter.internal.Endpoint;
import ai.zerok.javaagent.utils.Utils;
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
import java.util.logging.Logger;

import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.*;

public final class ZKSpanUtils {
    private static final Logger LOGGER = Utils.getLogger(ZKSpanUtils.class);
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
        LOGGER.config("spanId = " + spanData.getSpanId());
        LOGGER.config("traceId = " + spanData.getTraceId());
        LOGGER.config("parentSpanId = " + spanData.getParentSpanId());
        LOGGER.config("spanKind = " + spanData.getKind());
        LOGGER.config("spanAttributes = " + spanData.getTotalAttributeCount());
        LOGGER.config("Attributes = ");
        Map<AttributeKey<?>, Object> attributesMap = spanData.getAttributes().asMap();
        for(AttributeKey key: attributesMap.keySet()) {
            Object value = attributesMap.get(key);
            LOGGER.config("\t" + key + " = " + value.toString());
        }
        Endpoint remoteEndpoint = ZKSpanUtils.getRemoteEndpoint(spanData);
        LOGGER.config("Remote = " + remoteEndpoint);
        Endpoint localEndpoint = ZKSpanUtils.getLocalEndpoint(spanData);
        LOGGER.config("Local = " + localEndpoint);
    }
}
