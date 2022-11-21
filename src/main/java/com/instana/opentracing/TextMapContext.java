/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc.
 */
package com.instana.opentracing;

import java.util.Map;
import java.util.Set;

import io.opentracing.SpanContext;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapExtractAdapter;

class TextMapContext implements SpanContext {

  private final TextMapExtractAdapter baggageItems;

  TextMapContext(TextMapExtract carrier) {
    this.baggageItems = BaggageItemUtil.filterItems(carrier);
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    return baggageItems;
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
