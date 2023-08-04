package ai.zerok.javaagent.http;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;
import static ai.zerok.javaagent.utils.Utils.InitializeExtension;

@AutoService(InstrumentationModule.class)
public class HttpInstrumentationModule extends InstrumentationModule {
    public HttpInstrumentationModule() {
        super("java-http-zk");
        InitializeExtension();
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return asList(
            new HttpUrlConnectionInstrumentation()
           , new HttpClientInstrumentation()
        );
    }
    @Override
    public boolean isHelperClass(String className) {
        return className.equals("ai.zerok.javaagent.utils.Utils");
    }
}
