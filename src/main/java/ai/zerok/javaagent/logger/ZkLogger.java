package ai.zerok.javaagent.logger;

import ai.zerok.javaagent.utils.Utils;

public class ZkLogger {

    private static final String log_tag = "ZkLogger";
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


    private static final int debugLevel = 1;
    private static final String debugLabel = "DEBUG";
    private static final String debugColor = colorDebug;
    private static final int infoLevel = 2;
    private static final String infoLabel = "INFO";
    private static final String infoColor = colorInfo;
    private static final int warnLevel = 3;
    private static final String warnLabel = "WARN";
    private static final String warnColor = colorWarn;
    private static final int errorLevel = 4;
    private static final String errorLabel = "ERROR";
    private static final String errorColor = colorError;
    private static final int fatalLevel = 5;
    private static final String fatalLabel = "FATAL";
    private static final String fatalColor = colorFatal;

    private static int currentLevel = 1;


    private static boolean addColors = true;

    public static void setLogLevel(String logLevel) {
        error(log_tag,"Setting level to ",logLevel);
        if (logLevel == null) {
            return;
        }
        switch (logLevel) {
            case "DEBUG":
                currentLevel = debugLevel;
                break;
            case "INFO":
                currentLevel = infoLevel;
                break;
            case "WARN":
                currentLevel = warnLevel;
                break;
            case "ERROR":
                currentLevel = errorLevel;
                break;
            case "FATAL":
                currentLevel = fatalLevel;
                break;
        }
    }


    public static void debug(String tag, Object... messages) {
        log(debugLevel,debugLabel,debugColor, tag, messages);
    }


    public static void info(String tag, Object... messages) {
        log(infoLevel,infoLabel,infoColor, tag, messages);
    }


    public static void warn(String tag, Object... messages) {
        log(warnLevel,warnLabel,warnColor, tag, messages);
    }


    public static void error(String tag, Object... messages) {
        log(errorLevel,errorLabel,errorColor, tag, messages);
    }

    public static void fatal(String tag, Object... messages) {
        log(fatalLevel,fatalLabel,fatalColor, tag, messages);
    }

    private static void log(int level, String label , String color, String tag, Object... messages) {
        if (currentLevel <= level) {
            StringBuilder sb = new StringBuilder();
            String delimiter = " ";
            if (addColors) {
                sb.append(color).append("[").append(label).append("]").append(delimiter);
            } else {
                sb.append("[").append(label).append("] ").append(delimiter);
            }
            sb.append(Utils.getCurrentTime()).append(" |").append(delimiter);
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