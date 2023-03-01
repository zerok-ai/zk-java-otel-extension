package ai.zerok.javaagent.http;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.extendsClass;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;


import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class HttpClientInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<ClassLoader> classLoaderOptimization() {
        return hasClassesNamed("java.net.http.HttpClient");
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return nameStartsWith("java.net.")
                .or(nameStartsWith("jdk.internal."))
                .and(not(named("jdk.internal.net.http.HttpClientFacade")))
                .and(extendsClass(named("java.net.http.HttpClient")));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        System.out.println("Inside Exception ps- 1.2-http client....");

        transformer.applyAdviceToMethod(
                isMethod()
                        .and(named("send"))
                        .and(isPublic())
                        .and(takesArguments(2))
                        .and(takesArgument(0, named("java.net.http.HttpRequest"))),
                HttpClientInstrumentation.class.getName() + "$SendAdvice");

        transformer.applyAdviceToMethod(
                isMethod()
                        .and(named("sendAsync"))
                        .and(isPublic())
                        .and(takesArgument(0, named("java.net.http.HttpRequest")))
                        .and(takesArgument(1, named("java.net.http.HttpResponse$BodyHandler"))),
                HttpClientInstrumentation.class.getName() + "$SendAdvice");

        System.out.println("Inside Exception ps- 1.3-http client....");
    }

    public static class SendAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(@Advice.Argument(value = 0, readOnly = false) HttpRequest httpRequest) {
            CallDepth callDepth = CallDepth.forClass(HttpURLConnection.class);
            System.out.println("Http request instrumented."+ callDepth.getAndIncrement());
            Span span = Java8BytecodeBridge.currentSpan();
            String parentSpanId = Utils.getParentSpandId(span);
            String traceState = Utils.getTraceState(parentSpanId);
            if (traceState != null ){
                //httpRequest.headers().;
                httpRequest = HttpRequest.newBuilder(httpRequest, (n,v) -> true).setHeader(Utils.getTraceStateKey(),traceState).build();
            }
        }
    }
}

