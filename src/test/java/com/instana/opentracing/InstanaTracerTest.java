package com.instana.opentracing;

import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class InstanaTracerTest {

    @Test
    public void testTextMapExtraction() throws Exception {
        MapTextMap textMap = new MapTextMap();
        textMap.put("foo", "bar");
        SpanContext spanContext = InstanaTracerFactory.create().extract(Format.Builtin.TEXT_MAP, textMap);
        Iterator<Map.Entry<String, String>> iterator = spanContext.baggageItems().iterator();
        assertThat(iterator.hasNext(), is(true));
        Map.Entry<String, String> entry = iterator.next();
        assertThat(entry.getKey(), is("foo"));
        assertThat(entry.getValue(), is("bar"));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void testTextMapInjection() throws Exception {
        MapTextMap textMap = new MapTextMap();
        MapSpanContext spanContext = new MapSpanContext();
        spanContext.map.put("foo", "bar");
        InstanaTracerFactory.create().inject(spanContext, Format.Builtin.TEXT_MAP, textMap);
        assertThat(textMap.map.size(), is(1));
        assertThat(textMap.map.get("foo"), is("bar"));
    }

    @Test
    public void testHttpHeadersExtraction() throws Exception {
        MapTextMap textMap = new MapTextMap();
        textMap.put("foo", "bar");
        SpanContext spanContext = InstanaTracerFactory.create().extract(Format.Builtin.HTTP_HEADERS, textMap);
        Iterator<Map.Entry<String, String>> iterator = spanContext.baggageItems().iterator();
        assertThat(iterator.hasNext(), is(true));
        Map.Entry<String, String> entry = iterator.next();
        assertThat(entry.getKey(), is("foo"));
        assertThat(entry.getValue(), is("bar"));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void testHttpHeadersInjection() throws Exception {
        MapTextMap textMap = new MapTextMap();
        MapSpanContext spanContext = new MapSpanContext();
        spanContext.map.put("foo", "bar");
        InstanaTracerFactory.create().inject(spanContext, Format.Builtin.HTTP_HEADERS, textMap);
        assertThat(textMap.map.size(), is(1));
        assertThat(textMap.map.get("foo"), is("bar"));
    }

    @Test
    public void testByteBufferExtraction() throws Exception {
        byte[] key = "foo".getBytes(ByteBufferContext.CHARSET), value = "quxbaz".getBytes(ByteBufferContext.CHARSET);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 2 * 4 + key.length + value.length);
        byteBuffer.put(ByteBufferContext.ENTRY);
        byteBuffer.putInt(key.length);
        byteBuffer.putInt(value.length);
        byteBuffer.put(key);
        byteBuffer.put(value);
        byteBuffer.put(ByteBufferContext.NO_ENTRY);
        byteBuffer.flip();
        SpanContext spanContext = InstanaTracerFactory.create().extract(Format.Builtin.BINARY, byteBuffer);
        Iterator<Map.Entry<String, String>> iterator = spanContext.baggageItems().iterator();
        assertThat(iterator.hasNext(), is(true));
        Map.Entry<String, String> entry = iterator.next();
        assertThat(entry.getKey(), is("foo"));
        assertThat(entry.getValue(), is("quxbaz"));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void testByteBufferInjection() throws Exception {
        MapSpanContext spanContext = new MapSpanContext();
        spanContext.map.put("foo", "quxbaz");
        byte[] key = "foo".getBytes(ByteBufferContext.CHARSET), value = "quxbaz".getBytes(ByteBufferContext.CHARSET);
        ByteBuffer byteBuffer = ByteBuffer.allocate(2 + 2 * 4 + key.length + value.length);
        InstanaTracerFactory.create().inject(spanContext, Format.Builtin.BINARY, byteBuffer);
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

    private static class MapSpanContext implements SpanContext {

        final Map<String, String> map = new HashMap<String, String>();

        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            return map.entrySet();
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
}
