package ai.zerok.javaagent.http;

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

    private final String baseClassName = "javax.servlet.http.HttpServlet";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
//        return named("org.glassfish.jersey.servlet.ServletContainer");
        return extendsClass(named(baseClassName));
    }


    public void transform(TypeTransformer transformer) {
        System.out.println("Inside extends HttpServlet 1.11....");

        transformer.applyAdviceToMethod(
                named("service")
                        .and(ElementMatchers.isProtected()),
//                ServletContainerInstrumentation.class.getName() + "$HttpServletServiceAdvice");
                ServletContainerServiceAdvice.class.getName());

        System.out.println("Inside extends HttpServlet 1.22....");
    }

    @SuppressWarnings("unused")
    public static class ServletContainerServiceAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Argument(value = 0, readOnly = true) HttpServletRequest httpServletRequest,
                @Advice.Argument(value = 1, readOnly = false) HttpServletResponse httpServletResponse
        ) {
            System.out.println("Inside extends HttpServlet.service method...");
            httpServletResponse = HttpModifier.addTraceHeaders(httpServletRequest, httpServletResponse);
        }
    }

}