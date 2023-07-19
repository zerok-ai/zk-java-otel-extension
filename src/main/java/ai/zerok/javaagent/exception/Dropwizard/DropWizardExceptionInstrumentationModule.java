package ai.zerok.javaagent.exception.Dropwizard;

import ai.zerok.javaagent.exception.Dropwizard.DropwizardExceptionResolverInstrumentation;
import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

@AutoService(InstrumentationModule.class)
public final class DropWizardExceptionInstrumentationModule extends InstrumentationModule {
    public DropWizardExceptionInstrumentationModule() {
        super("dropwizard-exception");
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return Arrays.asList(
                new DropwizardExceptionResolverInstrumentation());
    }

    @Override
    public boolean isHelperClass(String className) {
        return
            className.equals("ai.zerok.javaagent.exception.ExceptionInstrumentation") ||
            className.equals("ai.zerok.javaagent.utils.Utils");
    }
}
