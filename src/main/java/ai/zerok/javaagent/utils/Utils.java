package ai.zerok.javaagent.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public final class Utils {

    public static final String operatorUrl = "http://zerok-operator-service.zerok-operator-system.svc.cluster.local:8127/exception";
    public static final String traceIdKey = "traceparent";

    public static String getTraceIdKey() {
         return traceIdKey;
    }

    public static int sendExceptionDataToOperator(Throwable throwable, String traceId, String spanId) {
        try {
            URL url = new URL(operatorUrl);
            URLConnection con = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)con;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", "application/json");
            String traceParent = getTraceParent(traceId, spanId);
            httpURLConnection.setRequestProperty(traceIdKey,traceParent);
            httpURLConnection.setDoOutput(true);

            String body = getExceptionPayload(throwable);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code " + responseCode);
            return responseCode;

        }
        catch (Throwable e) {
            System.out.println("Exception caught while sending exception data to operator."+e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return 500;
    }

    private static String getExceptionPayload(Throwable r) {
        HashMap<String,String> map = new HashMap<>();
        map.put("message",r.getMessage());
        String stackTraceString = Arrays.toString(r.getStackTrace());
        map.put("stacktrace",stackTraceString);
        System.out.println("json string is "+map);
        return map.toString();
    }

    public static String getTraceParent(String traceId, String spanId) {
        String sep = "-";
        return "00" + sep + traceId + sep + spanId + sep + "00";
    }
}
