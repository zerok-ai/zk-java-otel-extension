package ai.zerok.javaagent.http;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.net.HttpURLConnection;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class HttpUrlConnectionInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return extendsClass(named("java.net.HttpURLConnection"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
//        System.out.println("Http instrumentation 1.1");
//        transformer.applyAdviceToMethod(
//                isMethod().and(isPublic()).and(namedOneOf("connect", "getOutputStream", "getInputStream")),
//                this.getClass().getName() + "$HttpUrlConnectionAdvice");
//        System.out.println("Http instrumentation 1.2");
    }

    @SuppressWarnings("unused")
    public static class HttpUrlConnectionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(@Advice.This HttpURLConnection connection) {
//            CallDepth callDepth = CallDepth.forClass(HttpURLConnection.class);
//            System.out.println("Http url connection instrumented." + callDepth.getAndIncrement());
//            Span span = Java8BytecodeBridge.currentSpan();
//            String parentSpanId = Utils.getParentSpanId(span);
//            String traceState = Utils.getTraceState(parentSpanId);
//            if (traceState != null ){
//                //httpRequest.headers().;
//                System.out.println("Setting trace Id in request property.");
//                connection.setRequestProperty(Utils.getTraceStateKey(),traceState);
//            }
        }
    }
}
