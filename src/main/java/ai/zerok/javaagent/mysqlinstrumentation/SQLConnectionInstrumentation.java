/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.mysqlinstrumentation;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

import ai.zerok.javaagent.advice.CommentBuilder;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class SQLConnectionInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return implementsInterface(named("java.sql.Connection"));
  }

  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        nameStartsWith("prepareStatement")
            .and(ElementMatchers.takesArgument(0, String.class))
            .and(ElementMatchers.isPublic()),
        SQLConnectionInstrumentation.class.getName() + "$PrepareStatementAdvice");
  }

  @SuppressWarnings("unused")
  public static class PrepareStatementAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(@Advice.Argument(value = 0, readOnly = false) String sql) {
      sql =
          CommentBuilder.addCommentToQueryString(
              sql, "SQLConnectionInstrumentation_prepareStatement_prefix");
    }
  }
}
