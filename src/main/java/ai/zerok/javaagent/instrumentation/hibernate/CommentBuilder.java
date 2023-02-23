/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.zerok.javaagent.instrumentation.hibernate;

import ai.zerok.javaagent.utils.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;

import java.lang.reflect.Method;

public class CommentBuilder {

  private static final String queryIdentifierFormatter = "%s: ";
  private static final String traceIdFormatter = "%s tId:%s ";
  private static final String spanIdFormatter = "%s sId:%s";

  private static final String commentFormatter = "/* %s */ ";

  public static String addCommentToQueryString(String sql, String queryIdentifier) {
    Span span = Java8BytecodeBridge.currentSpan();

    String parentSpandId = Utils.getParentSpandId(span);

    String traceState = Utils.getTraceState(parentSpandId);

    SpanContext spanContext = span.getSpanContext();
    String traceParent = Utils.getTraceParent(spanContext.getTraceId(),spanContext.getSpanId());
    String comment = Utils.getTraceParentKey() + ":" +traceParent;
    if (traceState != null) {
      comment = comment + "," + Utils.getTraceStateKey() + ":" + traceState;
    }
    comment = formatComment(comment);
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

  public static String formatComment(String comment) {
    return String.format(commentFormatter, comment);
  }
}
