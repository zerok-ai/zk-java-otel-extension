package ai.zerok.javaagent.http.spring;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;
import static ai.zerok.javaagent.utils.Utils.InitializeExtension;

@AutoService(InstrumentationModule.class)
public class HttpServletInstrumentationModule extends InstrumentationModule {
    public HttpServletInstrumentationModule() {
        super("servlet-container");
        InitializeExtension();
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
                className.startsWith("ai.zerok.javaagent.utils")
                || className.equals("ai.zerok.javaagent.http.spring.HttpModifier");
    }

}