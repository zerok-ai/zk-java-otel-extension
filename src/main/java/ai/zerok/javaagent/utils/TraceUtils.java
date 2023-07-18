package ai.zerok.javaagent.utils;

public class TraceUtils {

    public static String appendToTraceState(String traceState, String key, String value){
        if(traceState == null){
            traceState = "";
        }
        if(traceState.length() > 0){
            traceState = traceState + "," + key+"="+value;
        }else{
            traceState = key+"="+value;
        }
        return traceState;
    }

    public static String appendToTraceState(String traceState, String keyValue){
        if(traceState == null){
            traceState = "";
        }
        if(traceState.length() > 0){
            traceState = traceState + "," + keyValue;
        }else{
            traceState = keyValue;
        }
        return traceState;
    }

    public static ZkTraceState extractZkTraceState(String traceState){
        ZkTraceState zkTraceState = null;
        if(traceState != null && traceState.length() > 0){
            zkTraceState = new ZkTraceState();
            String zkElementValue = "";
            String others = "";
            String[] elements = traceState.split(",");
            for(String element : elements){
                String[] keyValue = element.split("=");
                String key = keyValue[0];
                String value = keyValue[1];

                if(key.equals(Utils.getTraceStateZkKey())){
                    zkElementValue = value;
                }else{
                    if(others.length() > 0){
                        others = others + "," + element;
                    }else{
                        others = element;
                    }
                }
            }

            zkTraceState.setOthers(others);
            zkTraceState.setZkElementValue(zkElementValue);
        }

        return zkTraceState;
    }

    public static String extractTraceId(String traceparentHeader) {
        String[] parts = traceparentHeader.split("-");
        if (parts.length >= 2) {
            String traceId = parts[1];
            if (isValidHexadecimal(traceId)) {
                return traceId;
            }
        }
        return null;
    }

    public static String extractSpanId(String traceparentHeader) {
        String[] parts = traceparentHeader.split("-");
        if (parts.length >= 3) {
            String spanId = parts[2];
            if (isValidHexadecimal(spanId)) {
                return spanId;
            }
        }
        return null;
    }

    private static boolean isValidHexadecimal(String value) {
        try {
            Long.parseLong(value, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
