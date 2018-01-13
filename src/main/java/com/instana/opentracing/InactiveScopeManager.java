package com.instana.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

class InactiveScopeManager implements ScopeManager {

  @Override
  public Scope active() {
    return null;
  }

  @Override
  public Scope activate(Span span, boolean finishSpanOnClose) {
    throw new UnsupportedOperationException("The Instana tracer was not configured to use a scope manager");
  }
}
