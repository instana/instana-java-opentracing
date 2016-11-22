package com.instana.opentracing;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InstanaSpanBuilder implements Tracer.SpanBuilder {

    private final String operationName;

    private final Map<String, String> tags;

    private SpanContext parentContext;

    private long startTime;

    InstanaSpanBuilder(String operationName) {
        this.operationName = operationName;
        tags = new HashMap<String, String>();
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        parentContext = parent;
        return this;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(Span parent) {
        return asChildOf(parent.context());
    }

    @Override
    public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
        if (referenceType.equals(References.CHILD_OF)) {
            return asChildOf(referencedContext);
        } else {
            return this;
        }
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        if (key != null && value != null) {
            tags.put(key, value);
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        if (key != null) {
            tags.put(key, Boolean.toString(value));
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        if (key != null && value != null) {
            tags.put(key, value.toString());
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        startTime = microseconds;
        return this;
    }

    @Override
    public Span start() {
        return InstanaNoopSpan.INSTANCE;
    }

    @SuppressWarnings("unused")
    public Span doStart(Object dispatcher) {
        Span span = new InstanaSpan(dispatcher, baggageItems()).considerStart(startTime).setOperationName(operationName);
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            span.setTag(tag.getKey(), tag.getValue());
        }
        return span;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return parentContext == null
                ? Collections.<String, String>emptyMap().entrySet()
                : parentContext.baggageItems();
    }
}
