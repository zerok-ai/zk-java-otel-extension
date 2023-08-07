package ai.zerok.javaagent.logger;

public class LogsConfig {
    public static boolean isColor() {
        return color;
    }

    public static void setColor(boolean color) {
        LogsConfig.color = color;
    }

    public static String getLevel() {
        return level;
    }

    public static void setLevel(String level) {
        LogsConfig.level = level;
    }

    private  static boolean color = true;
    private static String level = "FATAL";
}

