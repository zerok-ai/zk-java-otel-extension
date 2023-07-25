package ai.zerok.javaagent.http;

import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.net.HttpURLConnection;
import java.util.logging.Logger;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class HttpUrlConnectionInstrumentation implements TypeInstrumentation {
    private static final Logger LOGGER = Utils.getLogger(HttpUrlConnectionInstrumentation.class);
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named("java.net.HttpURLConnection"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        LOGGER.fine("Http instrumentation 1.1");
        transformer.applyAdviceToMethod(
                isMethod().and(isPublic()).and(namedOneOf("connect", "getOutputStream", "getInputStream")),
                this.getClass().getName() + "$HttpUrlConnectionAdvice");
        LOGGER.fine("Http instrumentation 1.2");
    }

    @SuppressWarnings("unused")
    public static class HttpUrlConnectionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(@Advice.This HttpURLConnection connection) {
            CallDepth callDepth = CallDepth.forClass(HttpURLConnection.class);
            LOGGER.fine("Http url connection instrumented." + callDepth.getAndIncrement());
            Span span = Java8BytecodeBridge.currentSpan();
            String parentSpanId = Utils.getParentSpandId(span);
            String traceState = Utils.getTraceState(parentSpanId);
            if (traceState != null ){
                //httpRequest.headers().;
                connection.setRequestProperty(Utils.getTraceStateKey(),traceState);
            }
        }
    }
}
