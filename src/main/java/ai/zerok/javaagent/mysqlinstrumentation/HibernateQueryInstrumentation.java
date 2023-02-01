/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.mysqlinstrumentation;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.*;

import ai.zerok.javaagent.instrumentation.hibernate.CommentBuilder;
import ai.zerok.javaagent.instrumentation.hibernate.HibernateOperation;
import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.hibernate.Query;

public class HibernateQueryInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return implementsInterface(named("org.hibernate.Query"));
  }

  public void transform(TypeTransformer transformer) {

    transformer.applyAdviceToMethod(
        isMethod().and(named("setComment")), GetQueryStringAdvice.class.getName());
  }

  @SuppressWarnings("unused")
  public static class GetQueryStringAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void startMethod(
        @Advice.Local("otelCallDepth") CallDepth callDepth,
        @Advice.Argument(value = 0, readOnly = false) String sql,
        @Advice.Local("otelHibernateOperation") HibernateOperation hibernateOperation,
        @Advice.This Query query) {
      callDepth = CallDepth.forClass(HibernateOperation.class);
      if (callDepth.getAndIncrement() > 0) {
        return;
      }
      sql =
          CommentBuilder.addCommentToQueryString(
              sql, "HibernateQueryInstrumentation_getQueryString");
    }
  }
}
