package com.instana.opentracing;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.Span;
import io.opentracing.SpanContext;

public class InstanaSpan implements Span, SpanContext {

  private static final long NO_TIME = 0L;

  @SuppressWarnings("unused")
  private final Object dispatcher;

  private final Map<String, String> baggageItems;

  InstanaSpan(Object dispatcher, Iterable<Map.Entry<String, String>> baggageItems) {
    this.dispatcher = dispatcher;
    this.baggageItems = new HashMap<String, String>();
    if (baggageItems != null) {
      for (Map.Entry<String, String> baggageItem : baggageItems) {
        this.baggageItems.put(baggageItem.getKey(), baggageItem.getValue());
      }
    }
  }

  @SuppressWarnings("unused")
  InstanaSpan considerStart(long time) {
    if (time == NO_TIME) {
      return this;
    } else {
      return start(time);
    }
  }

  @SuppressWarnings("unused")
  private InstanaSpan start(long time) {
    return this;
  }

  @Override
  public SpanContext context() {
    return this;
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    return baggageItems.entrySet();
  }

  @Override
  public void finish() {
  }

  @Override
  public void finish(long finishMicros) {
  }

  @Override
  public Span setTag(String key, String value) {
    return this;
  }

  @Override
  public Span setTag(String key, boolean value) {
    return this;
  }

  @Override
  public Span setTag(String key, Number value) {
    return this;
  }

  @Override
  public Span log(Map<String, ?> fields) {
    return log(System.currentTimeMillis(), fields);
  }

  @Override
  public Span log(long timestampMicroseconds, Map<String, ?> fields) {
    for (Map.Entry<String, ?> entry : fields.entrySet()) {
      log(timestampMicroseconds, entry.getKey(), entry.getValue());
    }
    return this;
  }

  @Override
  public Span log(String event) {
    return log(System.currentTimeMillis(), event, event);
  }

  @Override
  public Span log(long timestampMicroseconds, String event) {
    return log(timestampMicroseconds, event, event);
  }

  @Override
  public Span setBaggageItem(String key, String value) {
    baggageItems.put(key, value);
    return this;
  }

  @Override
  public String getBaggageItem(String key) {
    return baggageItems.get(key);
  }

  @Override
  public Span setOperationName(String operationName) {
    return this;
  }

  private Span log(long timestampMicroseconds, String eventName, Object payload) {
    if (payload == null) {
      return this;
    }
    return setTag("log." + timestampMicroseconds + "." + eventName, payload.toString());
  }
}
