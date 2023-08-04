package ai.zerok.javaagent.logger;

public interface ZkLogger {
    void debug(String tag, Object... messages);

    void info(String tag, Object... messages);

    void warn(String tag, Object... messages);

    void error(String tag, Object... messages);

    void fatal(String tag, Object... messages);
}

