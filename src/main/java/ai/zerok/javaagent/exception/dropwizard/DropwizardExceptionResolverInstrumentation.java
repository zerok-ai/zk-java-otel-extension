package ai.zerok.javaagent.exception.dropwizard;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Arrays;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class DropwizardExceptionResolverInstrumentation implements TypeInstrumentation {

    private final String interfaceName = "javax.ws.rs.ext.ExceptionMapper";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return implementsInterface(nameStartsWith(interfaceName));
    }


    public void transform(TypeTransformer transformer) {
        System.out.println("Inside Exception ps- 1.2-dropwizard....");

        transformer.applyAdviceToMethod(
                named("toResponse"),
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
            for(int i=0;i<args.length;i++) {
                Object arg = args[i];
                if(arg instanceof Throwable) {
                    Throwable exception = (Throwable) arg;
                    System.out.println("The stacktrace is "+ Arrays.toString(exception.getStackTrace()));
                    break;
                }
            }
        }
    }
}
