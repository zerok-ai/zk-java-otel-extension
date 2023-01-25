package ai.zerok.javaagent.exception.Dropwizard;

import ai.zerok.javaagent.exception.Utils;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class DropwizardExceptionResolverInstrumentation implements TypeInstrumentation {


    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.glassfish.jersey.server.ServerRuntime$Responder");
    }


    public void transform(TypeTransformer transformer) {
        System.out.println("Inside Exception ps- 1.2-dropwizard....");

        transformer.applyAdviceToMethod(
                named("mapException"),
                DropwizardExceptionResolverInstrumentation.class.getName() + "$DropWizardExceptionAdvice");

        System.out.println("Inside Exception ps- 1.3-dropwizard....");
    }

    @SuppressWarnings("unused")
    public static class DropWizardExceptionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] args
        ) {
            System.out.println("Caught exception in dropwizard handler in agent.");
            String traceId = Java8BytecodeBridge.currentSpan().getSpanContext().getTraceId();
            for(int i=0;i<args.length;i++) {
                Object arg = args[i];
                if(arg instanceof Throwable) {
                    Throwable exception = (Throwable) arg;
                    exception = exception.getCause();
                    System.out.println("The stacktrace is "+ Arrays.toString(exception.getStackTrace()));
                    System.out.println("TraceId is "+traceId);
                    int responsecode = Utils.sendExceptionDataToOperator(exception,traceId);
                    break;
                }
            }
        }
    }
}
