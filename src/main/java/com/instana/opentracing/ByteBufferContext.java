/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc.
 */
package com.instana.opentracing;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.propagation.BinaryExtract;

class ByteBufferContext implements SpanContext {

  static final Charset CHARSET = Charset.forName("UTF-8");

  static final byte NO_ENTRY = 0, ENTRY = 1;

  private final ByteBuffer byteBuffer;

  ByteBufferContext(BinaryExtract carrier) {
    this.byteBuffer = carrier.extractionBuffer();
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    Map<String, String> baggageItems = new HashMap<String, String>();
    while (byteBuffer.get() == ENTRY) {
      byte[] key = new byte[byteBuffer.getInt()], value = new byte[byteBuffer.getInt()];
      byteBuffer.get(key);
      byteBuffer.get(value);
      baggageItems.put(new String(key, CHARSET), new String(value, CHARSET));
    }
    return baggageItems.entrySet();
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
