package ai.zerok.javaagent.http.spring;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

@AutoService(InstrumentationModule.class)
public class HttpServletInstrumentationModule extends InstrumentationModule {

    private static final String log_tag = "SpringHttpServerModule";
    public HttpServletInstrumentationModule() {
        super("servlet-container");
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return Arrays.asList(
                new HttpServletInstrumentation());
    }

    @Override
    public boolean isHelperClass(String className) {
        return
                className.startsWith("ai.zerok.javaagent.utils") ||
                className.equals("ai.zerok.javaagent.logger.ZkLogger") ||
                className.equals("ai.zerok.javaagent.logger.LogsConfig")
                || className.equals("ai.zerok.javaagent.http.spring.HttpModifier");
    }

}