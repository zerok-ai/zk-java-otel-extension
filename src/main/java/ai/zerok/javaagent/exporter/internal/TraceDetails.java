package ai.zerok.javaagent.exporter.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TraceDetails implements Serializable {
    private Map<String, SpanDetails> SpanDetailsMap = new HashMap<>();

    public Map<String, SpanDetails> setSpanDetails(String spanId, SpanDetails spanDetails) {
        SpanDetailsMap.put(spanId, spanDetails);
        return SpanDetailsMap;
    }

    public SpanDetails getSpanDetails(String spanId) {
        return SpanDetailsMap.get(spanId);
    }

    public Map<String, SpanDetails> getSpanDetailsMap() {
        return SpanDetailsMap;
    }

    public void setSpanDetailsMap(Map<String, SpanDetails> spanDetailsMap) {
        SpanDetailsMap = spanDetailsMap;
    }

    @Override
    public String toString() {
        String returnStr = "TraceDetails[";
        for (String spanId: SpanDetailsMap.keySet() ) {
            returnStr = returnStr + "{"
                + "spanId=" + spanId + ", "
                + "SpanDetails=" + SpanDetailsMap.get(spanId).toString()
                + "} ";
        }
        returnStr += "]";

        return returnStr;
    }
}
