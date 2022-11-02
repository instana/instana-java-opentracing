/*
 * (c) Copyright IBM Corp. 2022
 */
package com.instana.opentracing;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.propagation.BinaryExtract;
import io.opentracing.propagation.TextMapExtract;

class InstanaSpanContext implements SpanContext {

  static final Charset CHARSET = Charset.forName("UTF-8");

  static final byte NO_ENTRY = 0, ENTRY = 1;

  // In general, headers follow multi-map semantics.
  // Using a Map implies that whenever an Instana header (unexpectedly) appears multiple times, 
  // the last seen header value takes precedence and overwrites any previous values.
  private final Map<String, String> items;

  InstanaSpanContext(TextMapExtract carrier) {
    this.items = new HashMap<String, String>();
    for (Map.Entry<String, String> item : carrier) {
      String key = item.getKey();
      String value = item.getValue();
      putInstanaItem(key, value);
    }
  }

  InstanaSpanContext(BinaryExtract carrier) {
    this.items = new HashMap<String, String>();
    ByteBuffer byteBuffer = carrier.extractionBuffer();
    while (byteBuffer.get() == ENTRY) {
      byte[] rawKey = new byte[byteBuffer.getInt()];
      byte[] rawValue = new byte[byteBuffer.getInt()];
      byteBuffer.get(rawKey);
      byteBuffer.get(rawValue);
      String key = new String(rawKey, CHARSET);
      String value = new String(rawValue, CHARSET);
      putInstanaItem(key, value);
    }
  }
  
  private void putInstanaItem(String rawKey, String value) {
    // HTTP headers are case-insensitive, hence the key gets canonicalized to its lower-case version
    String key = rawKey.toLowerCase();
    if ("x-instana-t".equals(key) ||
        "x-instana-s".equals(key) ||
        "x-instana-l".equals(key) ||
        "traceparent".equals(key) ||
        "tracestate".equals(key)) {
      this.items.put(key, value);
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
