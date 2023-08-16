package ai.zerok.javaagent.exporter.internal;

import com.google.gson.JsonObject;
import io.opentelemetry.api.trace.SpanKind;

public class SpanDetails {
    private SpanKind spanKind;
    private String parentSpanID;
    private String endpoint;
    private JsonObject attributes;
    private long start_ns;
    private long end_ns;

    public SpanKind getSpanKind() { return spanKind; }
    public void setSpanKind(SpanKind value) { this.spanKind = value; }

    public String getParentSpanID() { return parentSpanID; }
    public void setParentSpanID(String value) { this.parentSpanID = value; }

    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public JsonObject getAttributes() {
        return attributes;
    }
    public void setAttributes(JsonObject attributes) {
        this.attributes = attributes;
    }

    public long getStart_ns() {
        return start_ns;
    }

    public void setStart_ns(long start_ns) {
        this.start_ns = start_ns;
    }

    public long getEnd_ns() {
        return end_ns;
    }

    public void setEnd_ns(long end_ns) {
        this.end_ns = end_ns;
    }

    @Override
    public String toString() {
        return "SpanDetails {"
            + "spanKind=" + spanKind + ", "
            + "parentSpanID=" + parentSpanID + ", "
            + "start_ns=" + start_ns + ", "
            + "end_ns=" + end_ns + ", "
            + "endpoint=" + endpoint + ", "
            + "attributes=" + attributes
            + "}";
    }

}
