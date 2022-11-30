package io.frictionlessdata.tableschema.schema;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MapValueComparator implements Comparator<String> {

    Map<String, Integer> map = new HashMap<>();

    public MapValueComparator(Map<String, Integer> map) {
        this.map.putAll(map);
    }

    @Override
    public int compare(String s1, String s2) {
        return map.get(s1).compareTo(map.get(s2));
    }
}
