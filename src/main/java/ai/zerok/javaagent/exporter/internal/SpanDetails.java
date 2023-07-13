package ai.zerok.javaagent.exporter.internal;

import io.opentelemetry.api.trace.SpanKind;

public class SpanDetails {
    private SpanKind spanKind;
    private String parentSpanID;
    private Endpoint remoteEndpoint; // Remote endpoint for the current span. i.e. destination of the request.
    private Endpoint localEndpoint; // Local endpoint where span is created. i.e. source of the request.
    private String protocol;

    public SpanKind getSpanKind() { return spanKind; }
    public void setSpanKind(SpanKind value) { this.spanKind = value; }

    public String getParentSpanID() { return parentSpanID; }
    public void setParentSpanID(String value) { this.parentSpanID = value; }

    public Endpoint getRemoteEndpoint() { return remoteEndpoint; }
    public void setRemoteEndpoint(Endpoint value) { this.remoteEndpoint = value; }

    public Endpoint getLocalEndpoint() { return localEndpoint; }
    public void setLocalEndpoint(Endpoint value) { this.localEndpoint = value; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String value) { this.protocol = value; }

    @Override
    public String toString() {
        return "SpanDetails {"
            + "spanKind=" + spanKind + ", "
            + "protocol=" + protocol + ", "
            + "parentSpanID=" + parentSpanID + ", "
            + "remoteEndpoint=" + remoteEndpoint.toString() + ", "
            + "localEndpoint=" + localEndpoint.toString()
            + "}";
    }

}
