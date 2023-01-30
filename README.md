# ZK OTEL EXTENSION
***

## Introduction
***
An OpenTelemetry extension created to gather telemetry data of outgoing request which are natively not tracked by OpenTelemetry.

## Prerequisite
***
OpenTelemetry java agent jar should be available. This could be downloaded from [Otel Java JAR](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar).

## Build and add extensions
***
To build this extension project, run [formatAndBuild.sh](formatAndBuild.sh). You can find the resulting jar file [zk-otel-extension-1.0-all.jar](build%2Flibs%2Fzk-otel-extension-1.0-all.jar) at _build/libs_.

To add the extension to the instrumentation agent:

1. Copy the jar file to a host that is running an application to which you've attached the OpenTelemetry Java instrumentation.
2. Modify the startup command to add the full path to the extension file. For example:
    

    java -javaagent:path/to/opentelemetry-javaagent.jar \
    -Dotel.javaagent.extensions=path/to/zk-otel-extension-1.0-all.jar \
    -jar myapp.jar

Note: to load multiple extensions, you can specify a comma-separated list of extension jars or directories (that contain extension jars) for the otel.javaagent.extensions value.

## List of Extensions
***
The current extension provides traces for the below services:
1. Mysql 
    * Adds a comment as prefix to every query that goes out from the instrumented service.
    * The comment includes the following entries:
      * an identifier to identify the instrumented Class name and method name. Ex. `NativeProtocolInstrumentation_sendQueryString`, where **NativeProtocolInstrumentation** is instruemented class name and **sendQueryString** is the instrumented method in the class.
      * a trace Id. Example- `3f3889adaa86d673a6c39a30ff44626f`
      * a span Id. Example- `029402dbc24c51c5`
    * The final query post instrumentation looks like this:
    
      `/* NativeProtocolInstrumentation_sendQueryString:  tId:3f3889adaa86d673a6c39a30ff44626f  sId:029402dbc24c51c5 */ insert into Employee (insert_time, NAME, ROLE) values (?, ?, ?)`