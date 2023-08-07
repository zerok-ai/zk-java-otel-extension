package ai.zerok.javaagent.http;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.*;

import ai.zerok.javaagent.logger.ZkLogger;
import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.net.http.HttpRequest;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class HttpClientInstrumentation implements TypeInstrumentation {

    private static final String log_tag = "HttpClientInstrumentation";

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named("java.net.http.HttpClient"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        ZkLogger.debug(log_tag ,"Inside ps- 1.2-http client....");

        transformer.applyAdviceToMethod(
                isMethod().and(isPublic()).and(namedOneOf("send", "sendAsync")),
                this.getClass().getName() + "$SendAdvice");

        ZkLogger.debug(log_tag ,"Inside ps- 1.3-http client....");
    }

    public static class SendAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(@Advice.Argument(value = 0, readOnly = false) HttpRequest httpRequest) {
            Span span = Java8BytecodeBridge.currentSpan();
            String parentSpanId = Utils.getParentSpandId(span);
            String traceState = Utils.getTraceState(parentSpanId);
            if (traceState != null ){
                //httpRequest.headers().;
                //httpRequest = HttpRequest.newBuilder(httpRequest, (n,v) -> true).header(Utils.getTraceStateKey(),traceState).build();
            }
        }
    }
}

