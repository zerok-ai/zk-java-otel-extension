package ai.zerok.javaagent.exception;


import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public final class Utils {

    public static final String operatorUrl = "http://zk-operator.zerok-operator-system.svc.cluster.local:8127";
    public static final String traceIdKey = "traceId";

    public static int sendExceptionDataToOperator(Throwable throwable, String traceId) {
        try {
            URL url = new URL(operatorUrl);
            URLConnection con = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)con;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", "application/json");
            httpURLConnection.setRequestProperty(traceIdKey,traceId);
            httpURLConnection.setDoOutput(true);

            String body = getExceptionPayload(throwable);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code " + responseCode);
            return responseCode;

        }
        catch (Exception e) {
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
}
