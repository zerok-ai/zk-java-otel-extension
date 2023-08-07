package ai.zerok.javaagent.http.spring;

import ai.zerok.javaagent.logger.ZkLogger;
import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.HttpURLConnection;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class HttpServletInstrumentation implements TypeInstrumentation {

    private static final String log_tag = "SprintHttpServlet";

    private final String baseClassName = "jakarta.servlet.http.HttpServlet";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named(baseClassName));
    }


    public void transform(TypeTransformer transformer) {
        ZkLogger.debug(log_tag,"Inside extends jakarta.HttpServlet 1.11....");
        javax.servlet.http.HttpServletResponse httpServletResponse2 = null;
        transformer.applyAdviceToMethod(
                named("service")
                        .and(ElementMatchers.isProtected()),
                ServletContainerServiceAdvice.class.getName());

        ZkLogger.debug(log_tag,"Inside extends jakarta.HttpServlet 1.22....");
    }

    @SuppressWarnings("unused")
    public static class ServletContainerServiceAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Argument(value = 0, readOnly = true) HttpServletRequest httpServletRequest,
                @Advice.Argument(value = 1, readOnly = false) HttpServletResponse httpServletResponse
        ) {
            StringBuilder endpoint = new StringBuilder(httpServletRequest.getRequestURL().toString());

            // Append query parameters if available
            String queryString = httpServletRequest.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                endpoint.append("?").append(queryString);
            }

            ZkLogger.debug(log_tag,"Inside extends HttpServlet.service method: ",endpoint);
            httpServletResponse = HttpModifier.addTraceHeaders(httpServletRequest, httpServletResponse);
        }
    }

}