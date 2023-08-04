package ai.zerok.javaagent.exception.Spring;

import ai.zerok.javaagent.exception.ExceptionInstrumentation;
import ai.zerok.javaagent.exception.ThreadLocalHelper;
import ai.zerok.javaagent.logger.ZkLogger;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class SpringExceptionResolverInstrumentation implements TypeInstrumentation {
    private static final String log_tag = "SpringException";

    private final String baseClassName = "org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver";
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named(baseClassName));
    }

    public void transform(TypeTransformer transformer) {
       ZkLogger.debug(log_tag,"Inside Exception ps- 1.2-spring....");

        transformer.applyAdviceToMethod(
                named("doResolveException"),
                SpringExceptionResolverInstrumentation.class.getName() + "$SprintBootExceptionAdvice");

        ZkLogger.debug(log_tag,"Inside Exception ps- 1.3-spring....");
    }

    @SuppressWarnings("unused")
    public static class SprintBootExceptionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.AllArguments(typing = Assigner.Typing.DYNAMIC) Object[] args
        ) {
            ZkLogger.debug("Caught exception in spring handler in agent.");
            for(int i=0;i<args.length;i++) {
                Object arg = args[i];
                if(arg instanceof Throwable) {
                    Throwable exception = (Throwable) arg;
                    int hashCode = exception.hashCode();
                    if(ThreadLocalHelper.getInstance().contains(hashCode)){
                        continue;
                    }
                    ThreadLocalHelper.getInstance().add(hashCode);
                    int responsecode = ExceptionInstrumentation.sendExceptionDataToOperator(exception,Java8BytecodeBridge.currentSpan());
                    break;
                }
            }
        }
    }
}
