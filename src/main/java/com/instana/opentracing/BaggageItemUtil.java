package com.instana.opentracing;

import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapExtractAdapter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.instana.opentracing.ByteBufferContext.CHARSET;
import static com.instana.opentracing.ByteBufferContext.ENTRY;

public class BaggageItemUtil {

  static TextMapExtractAdapter filterItems(TextMapExtract textMapExtract) {
    Map<String, String> baggageItems = new HashMap<String, String>();
    for (Map.Entry<String, String> entryItem : textMapExtract) {
      if (entryItem.getKey() == null) {
        continue;
      }
      if (canAddItem(entryItem.getKey())) {
        baggageItems.put(entryItem.getKey(), entryItem.getValue());
      }
    }
    return new TextMapExtractAdapter(baggageItems);
  }

  static Set<Map.Entry<String, String>> filterItems(ByteBuffer byteBuffer) {
    Map<String, String> baggageItems = new HashMap<String, String>();
    while (byteBuffer.get() == ENTRY) {
      byte[] key = new byte[byteBuffer.getInt()], value = new byte[byteBuffer.getInt()];
      byteBuffer.get(key);
      byteBuffer.get(value);
      final String stringKey = new String(key, CHARSET);
      if (canAddItem(stringKey)) {
        baggageItems.put(stringKey, new String(value, CHARSET));
      }
    }
    return baggageItems.entrySet();
  }

  private static boolean canAddItem(String key) {
    String lowerCaseKey = key.toLowerCase();
    return "x-instana-t".equals(lowerCaseKey)  //
        || "x-instana-s".equals(lowerCaseKey)  //
        || "x-instana-l".equals(lowerCaseKey)  //
        || "traceparent".equals(lowerCaseKey)  //
        || "tracestate".equals(lowerCaseKey);
  }
}
