package ai.zerok.javaagent.exception.springboot;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

@AutoService(InstrumentationModule.class)
public final class ExceptionInstrumentationModule extends InstrumentationModule {
    public ExceptionInstrumentationModule() {
        super("springboot-exception");
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return Arrays.asList(
                new ExceptionResolverInstrumentation());
    }

}
