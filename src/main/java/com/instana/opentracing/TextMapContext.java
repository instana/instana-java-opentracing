package com.instana.opentracing;

import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.propagation.TextMap;

class TextMapContext implements SpanContext {

  private final TextMap textMap;

  TextMapContext(TextMap textMap) {
    this.textMap = textMap;
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    return textMap;
  }
}
