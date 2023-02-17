package ai.zerok.javaagent.exception.Spring;

import ai.zerok.javaagent.exception.Spring.SpringExceptionResolverInstrumentation;
import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

@AutoService(InstrumentationModule.class)
public final class SpringExceptionInstrumentationModule extends InstrumentationModule {
    public SpringExceptionInstrumentationModule() {
        super("springboot-exception");
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return Arrays.asList(
                new SpringExceptionResolverInstrumentation());
    }

    @Override
    public boolean isHelperClass(String className) {
        return className.equals("ai.zerok.javaagent.utils.Utils");
    }

}
