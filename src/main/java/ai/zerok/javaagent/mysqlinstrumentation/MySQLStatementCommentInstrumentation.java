package ai.zerok.javaagent.mysqlinstrumentation;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

import io.opentelemetry.instrumentation.jdbc.internal.DbRequest;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.sql.Statement;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class MySQLStatementCommentInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    System.out.println("Inside MYSQL- 1.1....");
    return implementsInterface(named("java.sql.Statement"));
  }

  public void transform(TypeTransformer transformer) {
    System.out.println("Inside MYSQL- 1.2....");

    transformer.applyAdviceToMethod(
        nameStartsWith("execute")
            .and(ElementMatchers.takesArgument(0, String.class))
            .and(ElementMatchers.isPublic()),
        MySQLStatementCommentInstrumentation.class.getName() + "$StatementAddCommentAdvice");

    System.out.println("Inside MYSQL- 1.3");
  }

  @SuppressWarnings("unused")
  public static class StatementAddCommentAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(
        @Advice.Local("otelRequest") DbRequest request,
        @Advice.This Statement statement,
        @Advice.Argument(value = 0, readOnly = false) String sql) {
      System.out.println("Inside MYSQL- 1.4.");
      String traceId = Java8BytecodeBridge.currentSpan().getSpanContext().getTraceId();
      System.out.println(sql);
      String traceIdFormatter = "/* traceId: %s */ ";
      String comment = String.format(traceIdFormatter, traceId);
      sql = comment + sql;
      System.out.println(sql);
      request = DbRequest.create(statement, sql);
      System.out.println("Inside MYSQL- 1.5");
    }
  }
}
