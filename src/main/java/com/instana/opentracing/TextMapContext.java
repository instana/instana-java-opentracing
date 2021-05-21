/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc.
 */
package com.instana.opentracing;

import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.propagation.TextMapExtract;

class TextMapContext implements SpanContext {

  private final TextMapExtract textMap;

  TextMapContext(TextMapExtract carrier) {
    this.textMap = carrier;
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    return textMap;
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
