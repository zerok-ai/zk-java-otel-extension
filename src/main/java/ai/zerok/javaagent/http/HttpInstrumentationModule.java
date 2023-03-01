package ai.zerok.javaagent.http;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;

@AutoService(InstrumentationModule.class)
public class HttpInstrumentationModule extends InstrumentationModule {
    public HttpInstrumentationModule() {
        super("java-http");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return asList(
                new HttpClientInstrumentation(),
                new HttpUrlConnectionInstrumentation());
    }
}
