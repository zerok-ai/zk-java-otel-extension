/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.mysqlinstrumentation;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;

import ai.zerok.javaagent.instrumentation.hibernate.CommentBuilder;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class DatabaseMetadataInstrumentation implements TypeInstrumentation {

  @Override
  public ElementMatcher<TypeDescription> typeMatcher() {
    return implementsInterface(named("java.sql.DatabaseMetaData"));
  }

  public void transform(TypeTransformer transformer) {
    transformer.applyAdviceToMethod(
        named("prepareMetaDataSafeStatement")
            .and(ElementMatchers.takesArgument(0, String.class))
            .and(ElementMatchers.isProtected()),
        PrepareMetaDataSafeStatementAdvice.class.getName());
  }

  @SuppressWarnings("unused")
  public static class PrepareMetaDataSafeStatementAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static void onEnter(@Advice.Argument(value = 0, readOnly = false) String sql) {
      sql =
          CommentBuilder.addCommentToQueryString(
              sql, "DatabaseMetadataInstrumentation_PrepareMetaDataSafeStatement");
    }
  }
}
