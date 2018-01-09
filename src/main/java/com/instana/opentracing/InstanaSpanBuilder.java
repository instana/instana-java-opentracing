package com.instana.opentracing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

public class InstanaSpanBuilder implements Tracer.SpanBuilder {

  private final ScopeManager scopeManager;

  private final String operationName;

  private final Map<String, String> tags;

  private boolean ignoreActiveSpan;

  private SpanContext parentContext;

  private long startTime;

  InstanaSpanBuilder(ScopeManager scopeManager, String operationName) {
    this.scopeManager = scopeManager;
    this.operationName = operationName;
    tags = new HashMap<String, String>();
  }

  @Override
  public Tracer.SpanBuilder asChildOf(SpanContext parent) {
    if (parent != null) {
      parentContext = parent;
    }
    return this;
  }

  @Override
  public Tracer.SpanBuilder asChildOf(Span parent) {
    if (parent != null) {
      parentContext = parent.context();
    }
    return this;
  }

  @Override
  public Tracer.SpanBuilder ignoreActiveSpan() {
    ignoreActiveSpan = true;
    return this;
  }

  @Override
  public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
    if (References.CHILD_OF.equals(referenceType)) {
      return asChildOf(referencedContext);
    } else if (References.FOLLOWS_FROM.equals(referenceType)) {
      // TODO: Currently the FOLLOWS_FROM semantic is not fully supported, but its better to have CHILD_OF than nothing
      return asChildOf(referencedContext);
    } else {
      return this;
    }
  }

  @Override
  public Tracer.SpanBuilder withTag(String key, String value) {
    if (key != null && value != null) {
      tags.put(key, value);
    }
    return this;
  }

  @Override
  public Tracer.SpanBuilder withTag(String key, boolean value) {
    if (key != null) {
      tags.put(key, Boolean.toString(value));
    }
    return this;
  }

  @Override
  public Tracer.SpanBuilder withTag(String key, Number value) {
    if (key != null && value != null) {
      tags.put(key, value.toString());
    }
    return this;
  }

  @Override
  public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
    startTime = microseconds;
    return this;
  }

  @Override
  public Span start() {
    return InstanaNoopSpan.INSTANCE;
  }

  @Override
  public Span startManual() {
    return start();
  }

  @Override
  public Scope startActive(boolean finishSpanOnClose) {
    return scopeManager.activate(start(), finishSpanOnClose);
  }

  @SuppressWarnings("unused")
  public Span doStart(Object dispatcher) {
    Span span = new InstanaSpan(dispatcher, baggageItems()).considerStart(startTime).setOperationName(operationName);
    for (Map.Entry<String, String> tag : tags.entrySet()) {
      span.setTag(tag.getKey(), tag.getValue());
    }
    return span;
  }

  private Iterable<Map.Entry<String, String>> baggageItems() {
    if (parentContext != null) { // prefer explicit parent
      return parentContext.baggageItems();
    } else if (!ignoreActiveSpan) {
      Scope scope = scopeManager.active();
      if (scope != null) {
        return scope.span().context().baggageItems();
      }
    }
    return Collections.<String, String> emptyMap().entrySet();
  }
}