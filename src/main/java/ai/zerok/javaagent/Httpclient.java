package ai.zerok.javaagent;


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import static ai.zerok.javaagent.Constants.operatorUrl;
import static ai.zerok.javaagent.Constants.traceIdKey;

public class Httpclient {

    public static void sendExceptionDataToOperator(Throwable throwable, String traceId) {
        try {
            URL url = new URL(operatorUrl);
            URLConnection con = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection)con;
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", "application/json");
            httpURLConnection.setRequestProperty(traceIdKey,traceId);
            httpURLConnection.setDoOutput(true);

            String body = Utils.getExceptionPayload(throwable);
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code " + responseCode);

        }
        catch (Exception e) {
            System.out.println("Exception caught while sending exception data to operator.");
            e.printStackTrace();
        }
    }
}
