package ai.zerok.javaagent.logger;

class LogLevel {
    private final int value;
    private final String label;
    private final String color;

    LogLevel(int value, String label, String color) {
        this.value = value;
        this.label = label;
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
