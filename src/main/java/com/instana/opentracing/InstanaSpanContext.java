/*
 * (c) Copyright IBM Corp. 2022
 */
package com.instana.opentracing;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.propagation.TextMapExtract;

class InstanaSpanContext implements SpanContext {

  private final Map<String, String> items;

  InstanaSpanContext(TextMapExtract carrier) {
    this.items = new HashMap<String, String>();
    for (Map.Entry<String, String> item : carrier) {
      String key = item.getKey().toLowerCase();
      String value = item.getValue();
      switch (key) {
        case "x-instana-t":
        case "x-instana-s":
        case "x-instana-l":
        case "traceparent":
        case "tracestate":
          this.items.put(key, value);
          break;          
      }
    }
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    return this.items.entrySet();
  }

  @Override
  public String toSpanId() {
    return "";
  }

  @Override
  public String toTraceId() {
    return "";
  }
}
