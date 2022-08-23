/*
 * (c) Copyright IBM Corp. 2021 (c) Copyright Instana Inc.
 */
package com.instana.opentracing;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.BinaryAdapters;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

public class InstanaTracerTest {

  private final InstanaTracer tracer = new InstanaTracer();

  @Test
  public void testTextMapExtraction() {
    testExtraction(Format.Builtin.TEXT_MAP);
  }

  @Test
  public void testTextMapInjection() {
    testInjection(Format.Builtin.TEXT_MAP);
  }

  @Test
  public void testHttpHeadersExtraction() {
    testExtraction(Format.Builtin.HTTP_HEADERS);
  }

  @Test
  public void testHttpHeadersInjection() {
    testInjection(Format.Builtin.HTTP_HEADERS);
  }

  private void testExtraction(Format<TextMap> format) {
    MapTextMap textMap = new MapTextMap();
    textMap.put("foo", "bar");
    SpanContext spanContext = tracer.extractContext(format, textMap);
    assertThat(spanContext.baggageItems(), Matchers.contains(isEntry("foo", "bar")));
  }

  private void testInjection(Format<TextMap> format) {
    MapTextMap textMap = new MapTextMap();
    MapSpanContext spanContext = new MapSpanContext();
    spanContext.map.put("foo", "bar");
    tracer.inject(spanContext, format, textMap);
    assertThat(textMap.map.size(), is(1));
    assertThat(textMap.map.get("foo"), is("bar"));
  }

  @Test
  public void testByteBufferExtraction() {
    byte[] key = "foo".getBytes(ByteBufferContext.CHARSET),
        value = "quxbaz".getBytes(ByteBufferContext.CHARSET);
    ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 2 * 4 + key.length + value.length);
    byteBuffer.put(ByteBufferContext.ENTRY);
    byteBuffer.putInt(key.length);
    byteBuffer.putInt(value.length);
    byteBuffer.put(key);
    byteBuffer.put(value);
    byteBuffer.put(ByteBufferContext.NO_ENTRY);
    byteBuffer.flip();
    SpanContext spanContext = tracer.extractContext(Format.Builtin.BINARY_EXTRACT,
        BinaryAdapters.extractionCarrier(byteBuffer));
    Iterator<Map.Entry<String, String>> iterator = spanContext.baggageItems().iterator();
    assertThat(iterator.hasNext(), is(true));
    Map.Entry<String, String> entry = iterator.next();
    assertThat(entry.getKey(), is("foo"));
    assertThat(entry.getValue(), is("quxbaz"));
    assertThat(iterator.hasNext(), is(false));
  }

  @Test
  public void testByteBufferInjection() {
    MapSpanContext spanContext = new MapSpanContext();
    spanContext.map.put("foo", "quxbaz");
    byte[] key = "foo".getBytes(ByteBufferContext.CHARSET),
        value = "quxbaz".getBytes(ByteBufferContext.CHARSET);
    ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 2 * 4 + key.length + value.length);
    tracer.inject(spanContext, Format.Builtin.BINARY_INJECT,
        BinaryAdapters.injectionCarrier(byteBuffer));
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

  @Test
  public void testServiceLoader() {
    Iterator<Tracer> services = ServiceLoader.load(Tracer.class).iterator();
    assertThat(services.hasNext(), is(true));
    assertThat(services.next(), instanceOf(InstanaTracer.class));
    assertThat(services.hasNext(), is(false));
  }

  private static class MapSpanContext implements SpanContext {

    final Map<String, String> map = new HashMap<String, String>();

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
      return map.entrySet();
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

  private static class MapTextMap implements TextMap {

    final Map<String, String> map = new HashMap<String, String>();

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
      return map.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
      map.put(key, value);
    }
  }

  public static Matcher<Map.Entry<String, String>> isEntry(String key, String value) {
    return CoreMatchers.allOf(
        Matchers.hasProperty("key", is(key)),
        Matchers.hasProperty("value", is(value)));
  }
}
