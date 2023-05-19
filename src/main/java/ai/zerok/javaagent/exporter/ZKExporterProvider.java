package ai.zerok.javaagent.exporter;


import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class ZKExporterProvider implements ConfigurableSpanExporterProvider {
    @Override
    public SpanExporter createExporter(ConfigProperties config) {
        ZKExporterBuilder builder = ZKExporter.builder();
        SpanExporter ZKExporter = builder.build();
        return ZKExporter;
    }

    @Override
    public String getName() {
        return "zerok";
    }
}


