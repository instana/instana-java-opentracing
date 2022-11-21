/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc.
 */
package com.instana.opentracing;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import io.opentracing.SpanContext;
import io.opentracing.propagation.BinaryExtract;

class ByteBufferContext implements SpanContext {

  static final Charset CHARSET = Charset.forName("UTF-8");

  static final byte NO_ENTRY = 0, ENTRY = 1;

  private final Set<Map.Entry<String, String>> baggageItems;

  ByteBufferContext(BinaryExtract carrier) {
    this.baggageItems = BaggageItemUtil.filterItems(carrier.extractionBuffer());
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
