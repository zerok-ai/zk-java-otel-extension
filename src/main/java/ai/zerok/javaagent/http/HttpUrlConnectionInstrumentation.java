package ai.zerok.javaagent.http;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static net.bytebuddy.matcher.ElementMatchers.not;

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
import net.bytebuddy.matcher.ElementMatchers;

public class HttpUrlConnectionInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return nameStartsWith("java.net.")
                .or(ElementMatchers.<TypeDescription>nameStartsWith("sun.net"))
                // In WebLogic, URL.openConnection() returns its own internal implementation of
                // HttpURLConnection, which does not delegate the methods that have to be instrumented to
                // the JDK superclass. Therefore, it needs to be instrumented directly.
                .or(named("weblogic.net.http.HttpURLConnection"))
                // This class is a simple delegator. Skip because it does not update its `connected`
                // field.
                .and(not(named("sun.net.www.protocol.https.HttpsURLConnectionImpl")))
                .and(extendsClass(named("java.net.HttpURLConnection")));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        System.out.println("Inside Exception ps- 1.2-http url con....");

        transformer.applyAdviceToMethod(
                isMethod().and(isPublic()).and(namedOneOf("connect", "getOutputStream", "getInputStream")),
                this.getClass().getName() + "$HttpUrlConnectionAdvice");

        System.out.println("Inside Exception ps- 1.2-http url con....");
    }

    @SuppressWarnings("unused")
    public static class HttpUrlConnectionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(@Advice.This HttpURLConnection connection) {
            CallDepth callDepth = CallDepth.forClass(HttpURLConnection.class);
            System.out.println("Http url connection instrumented." + callDepth.getAndIncrement());
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

