package ai.zerok.javaagent.http.dropwizard;

import ai.zerok.javaagent.logger.ZkLogger;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class HttpServletInstrumentation implements TypeInstrumentation {

    private static final String log_tag = "DropWizardHttpServlet";

    private final String baseClassName = "javax.servlet.http.HttpServlet";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named(baseClassName));
    }


    public void transform(TypeTransformer transformer) {
        ZkLogger.debug(log_tag,"Inside extends javax.HttpServlet 1.11....");

        transformer.applyAdviceToMethod(
                named("service")
                        .and(ElementMatchers.isProtected()),
                ServletContainerServiceAdvice.class.getName());

        ZkLogger.debug(log_tag,"Inside extends javax.HttpServlet 1.22....");
    }

    @SuppressWarnings("unused")
    public static class ServletContainerServiceAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Argument(value = 0, readOnly = true) HttpServletRequest httpServletRequest,
                @Advice.Argument(value = 1, readOnly = false) HttpServletResponse httpServletResponse
        ) {
            ZkLogger.debug(log_tag,"Inside extends HttpServlet.service method...");
            httpServletResponse = HttpModifier.addTraceHeaders(httpServletRequest, httpServletResponse);
        }
    }

}