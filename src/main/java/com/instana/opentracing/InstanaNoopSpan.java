package com.instana.opentracing;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Collections;
import java.util.Map;

class InstanaNoopSpan implements Span, SpanContext {

    static final Span INSTANCE = new InstanaNoopSpan();

    @Override
    public SpanContext context() {
        return this;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return Collections.<String, String>emptyMap().entrySet();
    }

    @Override
    public void finish() {
    }

    @Override
    public void finish(long finishMicros) {
    }

    @Override
    public Span setTag(String key, String value) {
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        return this;
    }

    @Override
    public Span setTag(String key, Number value) {
        return this;
    }

    @Override
    public Span log(Map<String, ?> fields) {
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        return this;
    }

    @Override
    public Span log(String event) {
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, String event) {
        return this;
    }

    @Override
    public Span setBaggageItem(String key, String value) {
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return null;
    }

    @Override
    public Span setOperationName(String operationName) {
        return this;
    }

    @Override
    public Span log(String eventName, Object payload) {
        return this;
    }

    @Override
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        return this;
    }
}
