/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.mysqlinstrumentation;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.*;

import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
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
        //        isMethod().and(namedOneOf("list", "executeUpdate", "uniqueResult", "iterate",
        // "scroll")),
        isMethod().and(named("getQueryString")),
        HibernateQueryInstrumentation.class.getName() + "$GetQueryStringAdvice");
  }

  @SuppressWarnings("unused")
  public static class GetQueryStringAdvice {
    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void startMethod(@Advice.This Query query) {
      String traceId = Java8BytecodeBridge.currentSpan().getSpanContext().getTraceId();
      query.setComment("HibernateQueryInstrumentation_getQueryString traceId: " + traceId);
      System.out.println("Inside hibernate Query instrumentation");
    }
  }
}
