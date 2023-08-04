package ai.zerok.javaagent.logger;

public class LogsConfig {

    private  boolean color = true;
    private  String level = "INFO";

    public  boolean isColor() {
        return color;
    }

    public  void setColor(boolean color) {
        this.color = color;
    }

    public  String getLevel() {
        return level;
    }

    public  void setLevel(String level) {
        this.level = level;
    }
}

