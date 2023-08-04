package ai.zerok.javaagent.exception.Spring;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;
import static ai.zerok.javaagent.utils.Utils.InitializeExtension;

@AutoService(InstrumentationModule.class)
public final class SpringExceptionInstrumentationModule extends InstrumentationModule {
    public SpringExceptionInstrumentationModule() {
        super("springboot-exception");
        InitializeExtension();
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
        return
            className.equals("ai.zerok.javaagent.exception.ExceptionInstrumentation") ||
            className.equals("ai.zerok.javaagent.exception.ThreadLocalHelper") ||
            className.equals("ai.zerok.javaagent.utils.Utils") ||
            className.startsWith("ai.zerok.javaagent.exporter.internal") ||
            className.startsWith("ai.zerok.javaagent.utils.LRUCache") ||
            className.startsWith("org.apache.commons.pool2") ||
            className.startsWith("org.json") ||
            className.startsWith("com.google.gson") ||
            className.startsWith("redis.clients.jedis");
    }
}
