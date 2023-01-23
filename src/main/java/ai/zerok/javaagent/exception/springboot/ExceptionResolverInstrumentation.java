package ai.zerok.javaagent.exception.springboot;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import java.util.Arrays;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ExceptionResolverInstrumentation implements TypeInstrumentation {

    private final String baseClassName = "org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named(baseClassName));
    }


    public void transform(TypeTransformer transformer) {
        System.out.println("Inside Exception ps- 1.2-default....");

        transformer.applyAdviceToMethod(
                named("doResolveException"),
                ExceptionResolverInstrumentation.class.getName() + "$SprintBootExceptionAdvice");

        System.out.println("Inside Exception ps- 1.3-default....");
    }

    @SuppressWarnings("unused")
    public static class SprintBootExceptionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] args
        ) {
            System.out.println("Caught exception in default handler in agent.");
            for(int i=0;i<args.length;i++) {
                Object arg = args[i];
                if(arg instanceof Throwable) {
                    Exception exception = (Exception) arg;
                    System.out.println("The stacktrace is "+ Arrays.toString(exception.getStackTrace()));
                    break;
                }
            }
        }
    }
}
