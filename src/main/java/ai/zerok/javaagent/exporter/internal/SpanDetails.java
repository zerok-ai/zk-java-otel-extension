package ai.zerok.javaagent.exporter.internal;

public class SpanDetails {
    private String spanKind;
    private String parentSpanID;
    private Endpoint remoteEndpoint;
    private Endpoint localEndpoint;

    public String getSpanKind() { return spanKind; }
    public void setSpanKind(String value) { this.spanKind = value; }

    public String getParentSpanID() { return parentSpanID; }
    public void setParentSpanID(String value) { this.parentSpanID = value; }

    public Endpoint getRemoteEndpoint() { return remoteEndpoint; }
    public void setRemoteEndpoint(Endpoint value) { this.remoteEndpoint = value; }

    public Endpoint getLocalEndpoint() { return localEndpoint; }
    public void setLocalEndpoint(Endpoint value) { this.localEndpoint = value; }

    @Override
    public String toString() {
        return "SpanDetails {"
            + "spanKind=" + spanKind + ", "
            + "parentSpanID=" + parentSpanID + ", "
            + "remoteEndpoint=" + remoteEndpoint.toString() + ", "
            + "localEndpoint=" + localEndpoint.toString()
            + "}";
    }

}
