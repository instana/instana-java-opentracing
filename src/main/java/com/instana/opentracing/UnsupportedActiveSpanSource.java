package com.instana.opentracing;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;

class UnsupportedActiveSpanSource implements ActiveSpanSource {

    @Override
    public ActiveSpan activeSpan() {
        return null;
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        throw new UnsupportedOperationException("No active span source was set for Instana tracer");
    }
}
