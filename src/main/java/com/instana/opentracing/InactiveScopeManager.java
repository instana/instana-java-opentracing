/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc.
 */
package com.instana.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

class InactiveScopeManager implements ScopeManager {

  @Override
  public Span activeSpan() {
    return null;
  }

  @Override
  public Scope activate(Span span) {
    throw new UnsupportedOperationException("The Instana tracer was not configured to use a scope manager");
  }

}
