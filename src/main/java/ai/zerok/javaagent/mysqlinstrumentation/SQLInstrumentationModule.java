package ai.zerok.javaagent.mysqlinstrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import java.util.Arrays;
import java.util.List;

/**
 * This is a demo instrumentation which hooks into servlet invocation and modifies the http
 * response.
 */
@AutoService(InstrumentationModule.class)
public final class SQLInstrumentationModule extends InstrumentationModule {
  public SQLInstrumentationModule() {
    super("jdbc");
  }

  @Override
  public int order() {
    return 1;
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return Arrays.asList(
        new SQLStatementInstrumentation(),
        new SQLConnectionInstrumentation(),
        //        new HibernateQueryInstrumentation(),
        new MessageBuilderInstrumentation(),
        new NativeProtocolInstrumentation(),
        new DatabaseMetadataInstrumentation());
  }

  @Override
  public boolean isHelperClass(String className) {
    return className.startsWith("ai.zerok.javaagent.instrumentation.hibernate");
  }
}
