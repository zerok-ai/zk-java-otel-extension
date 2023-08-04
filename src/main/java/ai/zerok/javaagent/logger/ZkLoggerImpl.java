package ai.zerok.javaagent.logger;

public class ZkLoggerImpl implements ZkLogger {

    private static final String colorReset = "\033[0m";
    private static final String colorRed = "\033[31m";
    private static final String colorGreen = "\033[32m";
    private static final String colorYellow = "\033[33m";
    private static final String colorBlue = "\033[34m";
    private static final String colorPurple = "\033[35m";

    private static final String colorInfo = colorBlue;
    private static final String colorError = colorRed;
    private static final String colorWarn = colorYellow;
    private static final String colorDebug = colorGreen;
    private static final String colorFatal = colorPurple;

    private static final LogLevel _DEBUG_LEVEL = new LogLevel(1, "DEBUG", colorDebug);
    private static final LogLevel _INFO_LEVEL = new LogLevel(2, "INFO", colorInfo);
    private static final LogLevel _WARN_LEVEL = new LogLevel(3, "WARN", colorWarn);
    private static final LogLevel _ERROR_LEVEL = new LogLevel(4, "ERROR", colorError);
    private static final LogLevel _FATAL_LEVEL = new LogLevel(5, "FATAL", colorFatal);

    private LogLevel minLogLevel = _INFO_LEVEL;

    private boolean addColors = true;

    public ZkLoggerImpl(LogsConfig logsConfig) {
        switch (logsConfig.getLevel()) {
            case "DEBUG":
                this.minLogLevel = _DEBUG_LEVEL;
                break;
            case "INFO":
                this.minLogLevel = _INFO_LEVEL;
                break;
            case "WARN":
                this.minLogLevel = _WARN_LEVEL;
                break;
            case "ERROR":
                this.minLogLevel = _ERROR_LEVEL;
                break;
            case "FATAL":
                this.minLogLevel = _FATAL_LEVEL;
                break;
        }

        this.addColors = logsConfig.isColor();
    }

    @Override
    public void debug(String tag, Object... messages) {
        log(_DEBUG_LEVEL, tag, messages);
    }

    @Override
    public void info(String tag, Object... messages) {
        log(_INFO_LEVEL, tag, messages);
    }

    @Override
    public void warn(String tag, Object... messages) {
        log(_WARN_LEVEL, tag, messages);
    }

    @Override
    public void error(String tag, Object... messages) {
        log(_ERROR_LEVEL, tag, messages);
    }

    @Override
    public void fatal(String tag, Object... messages) {
        log(_FATAL_LEVEL, tag, messages);
    }

    private void log(LogLevel level, String tag, Object... messages) {
        if (minLogLevel.getValue() <= level.getValue()) {
            StringBuilder sb = new StringBuilder();
            String delimiter = " ";
            sb.append(System.currentTimeMillis()).append(" |").append(delimiter);
            if (addColors) {
                sb.append(level.getColor()).append("[").append(level.getLabel()).append("]").append(delimiter);
            } else {
                sb.append("[").append(level.getLabel()).append("] ").append(delimiter);
            }
            sb.append(tag).append(delimiter);
            sb.append("|").append(delimiter);
            for (Object message : messages) {
                sb.append(message).append(delimiter);
            }
            sb.append(colorReset).append(delimiter);
            System.out.println(sb);
        }
    }
}
