package com.instana.opentracing;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import java.nio.ByteBuffer;
import java.util.Map;

public class InstanaTracer implements Tracer {

    static Tracer INSTANCE = new InstanaTracer();

    public SpanBuilder buildSpan(String operationName) {
        return new InstanaSpanBuilder(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        if (format.equals(Format.Builtin.TEXT_MAP) || format.equals(Format.Builtin.HTTP_HEADERS)) {
            if (!(carrier instanceof TextMap)) {
                throw new IllegalArgumentException("Expected text map carrier: " + carrier);
            }
            for (Map.Entry<String, String> entry : spanContext.baggageItems()) {
                ((TextMap) carrier).put(entry.getKey(), entry.getValue());
            }
        } else if (format.equals(Format.Builtin.BINARY)) {
            if (!(carrier instanceof ByteBuffer)) {
                throw new IllegalArgumentException("Expected a byte buffer carrier: " + carrier);
            }
            for (Map.Entry<String, String> entry : spanContext.baggageItems()) {
                byte[] key = entry.getKey().getBytes(ByteBufferContext.CHARSET), value = entry.getValue().getBytes(ByteBufferContext.CHARSET);
                ((ByteBuffer) carrier).put(ByteBufferContext.ENTRY);
                ((ByteBuffer) carrier).putInt(key.length);
                ((ByteBuffer) carrier).putInt(value.length);
                ((ByteBuffer) carrier).put(key);
                ((ByteBuffer) carrier).put(value);
            }
            ((ByteBuffer) carrier).put(ByteBufferContext.NO_ENTRY);
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        if (format.equals(Format.Builtin.TEXT_MAP) || format.equals(Format.Builtin.HTTP_HEADERS)) {
            if (!(carrier instanceof TextMap)) {
                throw new IllegalArgumentException("Unsupported payload: " + carrier);
            }
            return new TextMapContext((TextMap) carrier);
        } else if (format.equals(Format.Builtin.BINARY)) {
            if (!(carrier instanceof ByteBuffer)) {
                throw new IllegalArgumentException("Unsupported payload: " + carrier);
            }
            return new ByteBufferContext((ByteBuffer) carrier);
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}
