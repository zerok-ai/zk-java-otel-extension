/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.advice;

import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

public class CommentBuilder {

  private static String commentFormatter = "/* %s tId: %s */ ";

  public static String addCommentToQueryString(String sql, String queryIdentifier) {
    String traceId = Java8BytecodeBridge.currentSpan().getSpanContext().getTraceId();
    String comment = String.format(commentFormatter, queryIdentifier, traceId);
    sql = comment + sql;
    System.out.println(sql);
    return sql;
  }
}
