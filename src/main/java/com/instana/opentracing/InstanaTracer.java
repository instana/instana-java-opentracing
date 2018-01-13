package com.instana.opentracing;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

/**
 * A tracer that becomes active when using Instana OpenTracing.
 */
public class InstanaTracer implements Tracer {

  private final ScopeManager scopeManager;

  /**
   * Creates a new Instana tracer with an implicit {@link ScopeManager} that is registered by the Java
   * {@link ServiceLoader}. If no scope manager is registered, this tracer will not offer support for active spans. To
   * set a scope manager explicitly, use {@link InstanaTracer#InstanaTracer(ScopeManager)}.
   */
  public InstanaTracer() {
    ScopeManager scopeManager = null;
    try {
      Iterator<ScopeManager> it = ServiceLoader.load(ScopeManager.class).iterator();
      if (it.hasNext()) {
        scopeManager = it.next();
      }
    } catch (Exception ignored) {
    }
    if (scopeManager == null) {
      this.scopeManager = new InactiveScopeManager();
    } else {
      this.scopeManager = scopeManager;
    }
  }

  /**
   * Creates a new Instana tracer.
   *
   * @param scopeManager
   *          The active span source to use.
   */
  public InstanaTracer(ScopeManager scopeManager) {
    this.scopeManager = scopeManager;
  }

  @Override
  public SpanBuilder buildSpan(String operationName) {
    return new InstanaSpanBuilder(scopeManager, operationName);
  }

  @Override
  public ScopeManager scopeManager() {
    return scopeManager;
  }

  @Override
  public Span activeSpan() {
    Scope active = scopeManager.active();
    if (active == null) {
      return null;
    } else {
      return active.span();
    }
  }

  @Override
  public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
    if (format.equals(Format.Builtin.TEXT_MAP) || format.equals(Format.Builtin.HTTP_HEADERS)) {
      if (!(carrier instanceof TextMap)) {
        throw new IllegalArgumentException("Expected text map carrier: " + carrier);
      }
      for (Map.Entry<String, String> entry : spanContext.baggageItems()) {
        ((TextMap) carrier).put(entry.getKey(), entry.getValue());
      }
    } else if (format.equals(Format.Builtin.BINARY)) {
      if (!(carrier instanceof ByteBuffer)) {
        throw new IllegalArgumentException("Expected a byte buffer carrier: " + carrier);
      }
      for (Map.Entry<String, String> entry : spanContext.baggageItems()) {
        byte[] key = entry.getKey().getBytes(ByteBufferContext.CHARSET),
            value = entry.getValue().getBytes(ByteBufferContext.CHARSET);
        ((ByteBuffer) carrier).put(ByteBufferContext.ENTRY);
        ((ByteBuffer) carrier).putInt(key.length);
        ((ByteBuffer) carrier).putInt(value.length);
        ((ByteBuffer) carrier).put(key);
        ((ByteBuffer) carrier).put(value);
      }
      ((ByteBuffer) carrier).put(ByteBufferContext.NO_ENTRY);
    } else {
      throw new IllegalArgumentException("Unsupported format: " + format);
    }
  }

  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {
    if (format.equals(Format.Builtin.TEXT_MAP) || format.equals(Format.Builtin.HTTP_HEADERS)) {
      if (!(carrier instanceof TextMap)) {
        throw new IllegalArgumentException("Unsupported payload: " + carrier);
      }
      return new TextMapContext((TextMap) carrier);
    } else if (format.equals(Format.Builtin.BINARY)) {
      if (!(carrier instanceof ByteBuffer)) {
        throw new IllegalArgumentException("Unsupported payload: " + carrier);
      }
      return new ByteBufferContext((ByteBuffer) carrier);
    } else {
      throw new IllegalArgumentException("Unsupported format: " + format);
    }
  }
}
