/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.instrumentation.hibernate;

import static ai.zerok.javaagent.instrumentation.hibernate.HibernateInstrumenterFactory.CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES;

import java.util.UUID;

public class SessionInfo {
  private final String sessionId;

  public SessionInfo() {
    sessionId = generateSessionId();
  }

  public String getSessionId() {
    return sessionId;
  }

  private static String generateSessionId() {
    if (!CAPTURE_EXPERIMENTAL_SPAN_ATTRIBUTES) {
      return null;
    }

    return UUID.randomUUID().toString();
  }
}
