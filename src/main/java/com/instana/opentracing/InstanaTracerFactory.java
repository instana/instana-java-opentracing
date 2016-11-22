package com.instana.opentracing;

import io.opentracing.Tracer;

/**
 * A factory for an open-tracing compliant tracer.
 */
public final class InstanaTracerFactory {

    /**
     * @return An open-tracing-compliant tracer.
     */
    public static Tracer create() {
        return InstanaTracer.INSTANCE;
    }

    private InstanaTracerFactory() {
        throw new UnsupportedOperationException();
    }
}
