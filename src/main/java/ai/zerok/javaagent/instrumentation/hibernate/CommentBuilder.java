/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.instrumentation.hibernate;

import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

public class CommentBuilder {

  private static final String queryIdentifierFormatter = "%s: ";
  private static final String traceIdFormatter = "%s tId:%s ";
  private static final String spanIdFormatter = "%s sId:%s";

  private static final String commentFormatter = "/* %s */ ";

  public static String addCommentToQueryString(String sql, String queryIdentifier) {
    SpanContext spanContext = Java8BytecodeBridge.currentSpan().getSpanContext();
    String traceParent = Utils.getTraceParent(spanContext.getTraceId(),spanContext.getSpanId());
    String comment = Utils.getTraceIdKey() + ":" +traceParent;
    comment = addComment(comment);
//    String comment =
//        addComment(
//            addSpanId(addTraceId(addQueryIdentifier(queryIdentifier), spanContext), spanContext));
    sql = comment + sql;
    System.out.println(sql);
    return sql;
  }

  public static String addQueryIdentifier(String queryIdentifier) {
    return String.format(queryIdentifierFormatter, queryIdentifier);
  }

  public static String addTraceId(String comment, SpanContext spanContext) {
    return String.format(traceIdFormatter, comment, spanContext.getTraceId());
  }

  public static String addSpanId(String comment, SpanContext spanContext) {
    return String.format(spanIdFormatter, comment, spanContext.getSpanId());
  }

  public static String addComment(String comment) {
    return String.format(commentFormatter, comment);
  }
}
