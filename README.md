# Instana Java OpenTracing&nbsp; [![Build Status](https://travis-ci.org/instana/instana-java-opentracing.svg?branch=master)](https://travis-ci.org/instana/instana-java-opentracing)

Instana is capable of collecting traces that are described via the [OpenTracing](http://opentracing.io) API. In order to collect such traces, this tracer implementation must be used.

The artifact is available on Maven Central. When using Maven you can include the tracer with:

```
<dependency>
  <groupId>com.instana</groupId>
  <artifactId>instana-java-opentracing</artifactId>
  <version>0.31.0</version>
</dependency>
```

The older 0.20.7 and 0.30.3 are also available.

The implementation's version number follows the [OpenTracing API version](https://github.com/opentracing/opentracing-java) that it implements.


The tracer is fully compliant with the OpenTracing API and is available via:

```java
io.opentracing.Tracer tracer = new InstanaTracer();
```
it will try to load a ScopeManager via the Java Service Loader.
Or when explicitly using a specific ScopeManager:

```java
io.opentracing.util.ThreadLocalScopeManager scopeManager = new ThreadLocalScopeManager();
io.opentracing.Tracer tracer = new InstanaTracer(scopeManager);
```

The Instana tracer supports context propagation using all of OpenTracing's built-in formats, i.e. `TextMap`, HTTP headers and `ByteBuffer`.

When the Instana monitoring agent is not attached, the Instana OpenTracing API will act as an inactive tracer, similarly to the [OpenTracing noop-tracer](https://github.com/opentracing/opentracing-java/tree/master/opentracing-noop). To activate opentracing you must activate it in the agent configuation:

```
# Java Tracing
com.instana.plugin.javatrace:
  instrumentation:
    # Lightweight Bytecode Instrumentation, enabled by default
    enabled: true
    # OpenTracing instrumentation, disabled by default
    opentracing: true
```
