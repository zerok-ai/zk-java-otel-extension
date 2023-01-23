package ai.zerok.javaagent.mysqlinstrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import java.util.Arrays;
import java.util.List;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * This is a demo instrumentation which hooks into servlet invocation and modifies the http
 * response.
 */
@AutoService(InstrumentationModule.class)
public final class MySQLCommentInstrumentationModule extends InstrumentationModule {
  public MySQLCommentInstrumentationModule() {
    super("jdbc");
  }

  @Override
  public int order() {
    return 1;
  }

  @Override
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    return AgentElementMatchers.hasClassesNamed("java.sql.Statement");
  }

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return Arrays.asList(
        new MySQLPreparedStatementCommentInstrumentation(),
        new MySQLStatementCommentInstrumentation());
  }

  //  public List<TypeInstrumentation> typeInstrumentations() {
  //    return singletonList(new MySQLAddCommentInstrumentation());
  //  }
}
