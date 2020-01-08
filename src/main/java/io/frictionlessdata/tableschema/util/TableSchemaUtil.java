package io.frictionlessdata.tableschema.util;

import java.util.HashMap;
import java.util.Map;

public class TableSchemaUtil {

    public static Map<Integer, Integer> createSchemaHeaderMapping(String[] headers, String[] sortedHeaders) {
        if ((null == headers) || (null == sortedHeaders))
            return null;
        Map<Integer, Integer> mapping = new HashMap<>();

        for (int i = 0; i < sortedHeaders.length; i++) {
            for (int j = 0; j < headers.length; j++) {
                if (sortedHeaders[i].equals(headers[j])) {
                    mapping.put(i, j);
                }
            }
            // declared header not found in actual data - can happen with JSON Arrays
            // of JSON objects as they will not have keys for null values
            if (!mapping.containsKey(i)) {
                mapping.put(i, null);
            }
        }
        return mapping;
    }
}
