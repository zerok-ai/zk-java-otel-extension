package ai.zerok.javaagent;

import org.json.simple.JSONObject;

public class Utils {
    public static String getExceptionPayload(Throwable r) {
            JSONObject json = new JSONObject();
            json.put("message",r.getMessage());
            json.put("stacktrace",r.getStackTrace());
            System.out.println("json string is "+json.toJSONString());
            return json.toJSONString();
    }
}
