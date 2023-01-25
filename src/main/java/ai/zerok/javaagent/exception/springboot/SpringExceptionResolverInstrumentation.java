package ai.zerok.javaagent.exception.springboot;

import ai.zerok.javaagent.Httpclient;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import java.util.Arrays;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class SpringExceptionResolverInstrumentation implements TypeInstrumentation {

    private final String baseClassName = "org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named(baseClassName));
    }


    public void transform(TypeTransformer transformer) {
        System.out.println("Inside Exception ps- 1.2-spring....");

        transformer.applyAdviceToMethod(
                named("doResolveException"),
                SpringExceptionResolverInstrumentation.class.getName() + "$SprintBootExceptionAdvice");

        System.out.println("Inside Exception ps- 1.3-spring....");
    }

    @SuppressWarnings("unused")
    public static class SprintBootExceptionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] args
        ) {
            System.out.println("Caught exception in spring handler in agent.");
            String traceId = Java8BytecodeBridge.currentSpan().getSpanContext().getTraceId();
            for(int i=0;i<args.length;i++) {
                Object arg = args[i];
                if(arg instanceof Throwable) {
                    Throwable exception = (Throwable) arg;
                    System.out.println("The stacktrace is "+ Arrays.toString(exception.getStackTrace()));
                    System.out.println("TraceId is "+traceId);
                    //Httpclient.sendExceptionDataToOperator(exception,traceId);
                    break;
                }
            }
        }
    }
}
