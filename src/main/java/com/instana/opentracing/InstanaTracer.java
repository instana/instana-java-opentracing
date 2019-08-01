package com.instana.opentracing;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.BinaryExtract;
import io.opentracing.propagation.BinaryInject;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapInject;

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
  public Scope activateSpan(Span span) {
    return scopeManager.activate(span);
  }

  @Override
  public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
    if (format.equals(Format.Builtin.TEXT_MAP) || format.equals(Format.Builtin.TEXT_MAP_INJECT)
        || format.equals(Format.Builtin.HTTP_HEADERS)) {
      if (!(carrier instanceof TextMapInject)) {
        throw new IllegalArgumentException("Expected text map carrier: " + carrier);
      }
      for (Map.Entry<String, String> entry : spanContext.baggageItems()) {
        ((TextMapInject) carrier).put(entry.getKey(), entry.getValue());
      }
    } else if (format.equals(Format.Builtin.BINARY) || format.equals(Format.Builtin.BINARY_INJECT)) {
      if (!(carrier instanceof BinaryInject)) {
        throw new IllegalArgumentException("Expected a byte buffer carrier: " + carrier);
      }
      int requiredSize = 1; // we end with a NO_ENTRY marker
      ArrayList<byte[]> binary = new ArrayList<byte[]>();
      for (Map.Entry<String, String> entry : spanContext.baggageItems()) {
        requiredSize += 1 + 4 + 4; // ENTRY marker + size of key and size of value
        byte[] key = entry.getKey().getBytes(ByteBufferContext.CHARSET);
        byte[] value = entry.getValue().getBytes(ByteBufferContext.CHARSET);
        requiredSize += key.length + value.length;
        binary.add(key);
        binary.add(value);
      }
      ByteBuffer injectionBuffer = ((BinaryInject) carrier).injectionBuffer(requiredSize);
      Iterator<byte[]> iterator = binary.iterator();
      while (iterator.hasNext()) {
        byte[] key = iterator.next();
        byte[] value = iterator.next();
        injectionBuffer.put(ByteBufferContext.ENTRY); // 1 byte
        injectionBuffer.putInt(key.length); // 4 bytes
        injectionBuffer.putInt(value.length); // 4 bytes
        injectionBuffer.put(key); // key.length
        injectionBuffer.put(value); // value.length

      }
      injectionBuffer.put(ByteBufferContext.NO_ENTRY);
    } else {
      throw new IllegalArgumentException("Unsupported format: " + format);
    }
  }

  @Override
  public <C> SpanContext extract(Format<C> format, C carrier) {
    if (format.equals(Format.Builtin.TEXT_MAP) || format.equals(Format.Builtin.TEXT_MAP_EXTRACT)
        || format.equals(Format.Builtin.HTTP_HEADERS)) {
      if (!(carrier instanceof TextMapExtract)) {
        throw new IllegalArgumentException("Unsupported payload: " + carrier);
      }
      return new TextMapContext((TextMapExtract) carrier);
    } else if (format.equals(Format.Builtin.BINARY) || format.equals(Format.Builtin.BINARY_EXTRACT)) {
      if (!(carrier instanceof BinaryExtract)) {
        throw new IllegalArgumentException("Unsupported payload: " + carrier);
      }
      return new ByteBufferContext((BinaryExtract) carrier);
    } else {
      throw new IllegalArgumentException("Unsupported format: " + format);
    }
  }

  @Override
  public void close() {
  }
}
