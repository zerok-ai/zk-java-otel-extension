package ai.zerok.javaagent.exporter.internal;

import ai.zerok.javaagent.exporter.ZKExporterBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.io.Serializable;

public class Endpoint implements Serializable{
    private String name;
    private String host;
    private String ipv6;
    private Long port;
    private String ipv4;

    @Override
    public String toString() {
        return "Endpoint{"
                + "Name=" + name + ", "
                + "host=" + host + ", "
                + "ipv4=" + ipv4 + ", "
                + "ipv6=" + ipv6 + ", "
                + "port=" + port
                + "}";
    }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getHost() { return host; }
    public void setHost(String value) { this.host = value; }

    public String getIpv6() { return ipv6; }
    public void setIpv6(String value) { this.ipv6 = value; }

    public Long getPort() { return port; }
    public void setPort(Long value) { this.port = value; }

    public String getIpv4() { return ipv4; }
    public void setIpv4(String value) { this.ipv4 = value; }
}

