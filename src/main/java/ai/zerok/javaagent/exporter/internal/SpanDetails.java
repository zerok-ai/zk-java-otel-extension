package ai.zerok.javaagent.exporter.internal;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.opentelemetry.api.trace.SpanKind;

public class SpanDetails {
    @SerializedName("span_kind")
    private SpanKind spanKind;
    @SerializedName("parent_span_id")
    private String parentSpanID;
    @SerializedName("endpoint")
    private String endpoint;
    @SerializedName("attributes")
    private JsonObject attributes;
    @SerializedName("start_ns")
    private long startNs;
    @SerializedName("end_ns")
    private long endNs;
    @SerializedName("source_ip")
    private String sourceIP;
    @SerializedName("dest_ip")
    private String destIP;

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

    public long getStartNs() {
        return startNs;
    }

    public void setStartNs(long startNs) {
        this.startNs = startNs;
    }

    public long getEndNs() {
        return endNs;
    }

    public void setEndNs(long endNs) {
        this.endNs = endNs;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public String getDestIP(String string) {
        return destIP;
    }

    public void setDestIP(String destIP) {
        this.destIP = destIP;
    }

    @Override
    public String toString() {
        return "SpanDetails {"
            + "spanKind=" + spanKind + ", "
            + "parentSpanID=" + parentSpanID + ", "
            + "startNs=" + startNs + ", "
            + "endNs=" + endNs + ", "
            + "endpoint=" + endpoint + ", "
            + "sourceIP=" + sourceIP + ", "
            + "destIP=" + destIP + ", "
            + "attributes=" + attributes
            + "}";
    }

}
