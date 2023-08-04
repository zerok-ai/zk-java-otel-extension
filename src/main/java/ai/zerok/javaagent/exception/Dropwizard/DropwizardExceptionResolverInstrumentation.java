package ai.zerok.javaagent.exception.Dropwizard;

import ai.zerok.javaagent.exception.ExceptionInstrumentation;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import static net.bytebuddy.matcher.ElementMatchers.named;
import ai.zerok.javaagent.logger.ZkLogger;

public class DropwizardExceptionResolverInstrumentation implements TypeInstrumentation {
    private static final String log_tag = "DropwizardException";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.glassfish.jersey.server.ServerRuntime$Responder");
    }


    public void transform(TypeTransformer transformer) {

      ZkLogger.debug(log_tag,"Inside Exception ps- 1.2-dropwizard....");

        transformer.applyAdviceToMethod(
                named("mapException"),
                DropwizardExceptionResolverInstrumentation.class.getName() + "$DropWizardExceptionAdvice");

       ZkLogger.debug(log_tag,"Inside Exception ps- 1.3-dropwizard....");
    }

    @SuppressWarnings("unused")
    public static class DropWizardExceptionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] args
        ) {
            ZkLogger.debug(log_tag,"Caught exception in dropwizard handler in agent.");
            Span span = Java8BytecodeBridge.currentSpan();

            for(int i=0;i<args.length;i++) {
                Object arg = args[i];
                if(arg instanceof Throwable) {
                    Throwable exception = (Throwable) arg;
                    exception = exception.getCause();
                    int responsecode = ExceptionInstrumentation.sendExceptionDataToOperator(exception,span);
                    break;
                }
            }
        }
    }
}
