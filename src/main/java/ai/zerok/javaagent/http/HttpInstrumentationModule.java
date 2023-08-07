package ai.zerok.javaagent.http;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.List;

import static java.util.Arrays.asList;

@AutoService(InstrumentationModule.class)
public class HttpInstrumentationModule extends InstrumentationModule {
    public HttpInstrumentationModule() {
        super("java-http-zk");
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
        return className.equals("ai.zerok.javaagent.utils.Utils") ||
        className.equals("ai.zerok.javaagent.logger.ZkLogger") ||
                className.equals("ai.zerok.javaagent.logger.LogsConfig") ;
    }
}
