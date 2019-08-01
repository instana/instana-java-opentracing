package com.instana.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

class InactiveScopeManager implements ScopeManager {

  @Override
  @Deprecated
  public Scope active() {
    return null;
  }

  @Override
  public Span activeSpan() {
    return null;
  }

  @Override
  @Deprecated
  public Scope activate(Span span) {
    throw new UnsupportedOperationException("The Instana tracer was not configured to use a scope manager");
  }

  @Override
  public Scope activate(Span span, boolean finishSpanOnClose) {
    throw new UnsupportedOperationException("The Instana tracer was not configured to use a scope manager");
  }

}
