/*
 * (c) Copyright IBM Corp. 2022
 * (c) Copyright Instana Inc.
 */
package com.instana.opentracing;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.BinaryAdapters;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class InstanaTracerTest {

  private final InstanaTracer tracer = new InstanaTracer();

  @Test public void testTextMapExtractionIgnoresIrrelevantHeaders() {
    testExtractionIgnoresIrrelevantHeaders(Format.Builtin.TEXT_MAP);
  }

  @Test public void testHttpHeadersExtractionIgnoresIrrelevantHeaders() {
    testExtractionIgnoresIrrelevantHeaders(Format.Builtin.HTTP_HEADERS);
  }

  @Test public void testTextMapExtractionKeepsInstanaHeaders() {
    testExtractionKeepsInstanaHeaders(Format.Builtin.TEXT_MAP);
  }

  @Test public void testHttpHeadersExtractionKeepsInstanaHeaders() {
    testExtractionKeepsInstanaHeaders(Format.Builtin.HTTP_HEADERS);
  }

  @Test public void testTextMapInjection() {
    testInjection(Format.Builtin.TEXT_MAP);
  }

  @Test public void testHttpHeadersInjection() {
    testInjection(Format.Builtin.HTTP_HEADERS);
  }

  private void testExtractionIgnoresIrrelevantHeaders(Format<TextMap> format) {
    MapTextMap textMap = new MapTextMap();
    textMap.put("foo", "bar"); // irrelevant custom header
    SpanContext spanContext = tracer.extractContext(format, textMap);
    assertThat(spanContext.baggageItems(), emptyIterable());
  }

  private void testExtractionKeepsInstanaHeaders(Format<TextMap> format) {
    MapTextMap textMap = new MapTextMap();
    textMap.put("foo", "bar"); // irrelevant header
    textMap.put("x-instana-t", "123");
    textMap.put("x-instana-s", "456");
    textMap.put("some", "thing"); // irrelevant header
    textMap.put("x-instana-l", "789");
    textMap.put("authorization", "bearer 000"); // irrelevant header
    textMap.put("traceparent", "abc");
    textMap.put("tracestate", "xyz");
    SpanContext spanContext = tracer.extractContext(format, textMap);
    assertThat(spanContext.baggageItems(),
        containsInAnyOrder(isEntry("x-instana-t", "123"), isEntry("x-instana-s", "456"), isEntry("x-instana-l", "789"),
            isEntry("traceparent", "abc"), isEntry("tracestate", "xyz")));
  }

  private void testExtractionIsCaseInsensitive(Format<TextMap> format) {
    MapTextMap textMap = new MapTextMap();
    textMap.put("X-INSTANA-T", "123");
    textMap.put("x-instana-s", "456");
    textMap.put("X-Instana-L", "789");
    SpanContext spanContext = tracer.extractContext(format, textMap);
    assertThat(spanContext.baggageItems(),
        containsInAnyOrder(isEntry("x-instana-t", "123"), isEntry("x-instana-s", "456"),
            isEntry("x-instana-l", "789")));
  }

  private void testInjection(Format<TextMap> format) {
    MapTextMap textMap = new MapTextMap();
    MapSpanContext spanContext = new MapSpanContext();
    spanContext.map.put("foo", "bar");
    tracer.inject(spanContext, format, textMap);
    assertThat(textMap.map.size(), is(1));
    assertThat(textMap.map.get("foo"), is("bar"));
  }

  @Test public void testByteBufferExtractionKeepsInstanaHeaders() {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("foo", "bar"); // irrelevant header
    headers.put("x-instana-t", "123");
    headers.put("x-instana-s", "456");
    headers.put("some", "thing"); // irrelevant header
    headers.put("x-instana-l", "789");
    headers.put("authorization", "bearer 000"); // irrelevant header
    headers.put("traceparent", "abc");
    headers.put("tracestate", "xyz");
    ByteBuffer byteBuffer = encodeToByteBuffer(headers);
    SpanContext spanContext = tracer.extractContext(Format.Builtin.BINARY_EXTRACT,
        BinaryAdapters.extractionCarrier(byteBuffer));
    assertThat(spanContext.baggageItems(),
        containsInAnyOrder(isEntry("x-instana-t", "123"), isEntry("x-instana-s", "456"), isEntry("x-instana-l", "789"),
            isEntry("traceparent", "abc"), isEntry("tracestate", "xyz")));
  }

  private ByteBuffer encodeToByteBuffer(Map<String, String> map) {
    int totalSize = byteBufferSize(map);
    ByteBuffer byteBuffer = ByteBuffer.allocate(totalSize);
    for (Map.Entry<String, String> entry : map.entrySet()) {
      byteBuffer.put(ByteBufferContext.ENTRY);
      byte[] key = entry.getKey().getBytes(ByteBufferContext.CHARSET);
      byte[] value = entry.getValue().getBytes(ByteBufferContext.CHARSET);
      byteBuffer.putInt(key.length);
      byteBuffer.putInt(value.length);
      byteBuffer.put(key);
      byteBuffer.put(value);
    }
    byteBuffer.put(ByteBufferContext.NO_ENTRY);
    byteBuffer.flip();
    return byteBuffer;
  }

  private int byteBufferSize(Map<String, String> map) {
    int totalSize = 0;
    for (Map.Entry<String, String> entry : map.entrySet()) {
      // ENTRY
      totalSize += 1;
      // key length int = 4 bytes
      totalSize += 4;
      // value length int = 4 bytes
      totalSize += 4;
      // key payload bytes
      totalSize += entry.getKey().getBytes(ByteBufferContext.CHARSET).length;
      // value payload bytes
      totalSize += entry.getValue().getBytes(ByteBufferContext.CHARSET).length;
    }
    // NO_ENTRY
    totalSize += 1;
    return totalSize;
  }

  @Test public void testByteBufferInjection() {
    MapSpanContext spanContext = new MapSpanContext();
    spanContext.map.put("foo", "quxbaz");
    byte[] key = "foo".getBytes(ByteBufferContext.CHARSET), value = "quxbaz".getBytes(ByteBufferContext.CHARSET);
    ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 2 * 4 + key.length + value.length);
    tracer.inject(spanContext, Format.Builtin.BINARY_INJECT, BinaryAdapters.injectionCarrier(byteBuffer));
    byteBuffer.flip();
    assertThat(byteBuffer.get(), is((byte) 1));
    assertThat(byteBuffer.getInt(), is(key.length));
    assertThat(byteBuffer.getInt(), is(value.length));
    byte[] readKey = new byte[key.length], readValue = new byte[value.length];
    byteBuffer.get(readKey);
    byteBuffer.get(readValue);
    assertThat(readKey, is(key));
    assertThat(readValue, is(value));
    assertThat(byteBuffer.get(), is((byte) 0));
  }

  @Test public void testServiceLoader() {
    Iterator<Tracer> services = ServiceLoader.load(Tracer.class).iterator();
    assertThat(services.hasNext(), is(true));
    assertThat(services.next(), instanceOf(InstanaTracer.class));
    assertThat(services.hasNext(), is(false));
  }

  private static class MapSpanContext implements SpanContext {

    final Map<String, String> map = new HashMap<String, String>();

    @Override public Iterable<Map.Entry<String, String>> baggageItems() {
      return map.entrySet();
    }

    @Override public String toSpanId() {
      return "";
    }

    @Override public String toTraceId() {
      return "";
    }
  }

  private static class MapTextMap implements TextMap {

    final Map<String, String> map = new HashMap<String, String>();

    @Override public Iterator<Map.Entry<String, String>> iterator() {
      return map.entrySet().iterator();
    }

    @Override public void put(String key, String value) {
      map.put(key, value);
    }
  }

  public static Matcher<Map.Entry<String, String>> isEntry(String key, String value) {
    return allOf(hasProperty("key", is(key)), hasProperty("value", is(value)));
  }
}
