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
            className.equals("ai.zerok.javaagent.exception.ThreadLocalHelper") ||
            className.equals("ai.zerok.javaagent.utils.Utils") ||
            className.startsWith("ai.zerok.javaagent.exporter.internal") ||
            className.startsWith("ai.zerok.javaagent.utils.LRUCache") ||
            className.startsWith("org.apache.commons.pool2") ||
            className.startsWith("org.json") ||
            className.startsWith("com.google.gson") ||
            className.startsWith("okhttp3") ||
            className.startsWith("okhttp3.OkHttpClient") ||
            className.startsWith("okhttp3.Request") ||
            className.startsWith("okhttp3.Response") ||
            className.startsWith("okhttp3.RequestBody") ||
            className.startsWith("okhttp3.internal.http.HttpHeaders") ||
            className.startsWith("okhttp3.WebSocket") ||
            className.startsWith("okhttp3.Cache") ||
            className.startsWith("okhttp3.Authenticator") ||
            className.startsWith("kotlin.collections.MapsKt") ||
            className.startsWith("redis.clients.jedis");
    }
}
