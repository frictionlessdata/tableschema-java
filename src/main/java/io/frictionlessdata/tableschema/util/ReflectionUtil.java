package io.frictionlessdata.tableschema.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReflectionUtil {

    public static BeanDescription getBeanDescription(Class<?> type) {
        CsvMapper mapper = new CsvMapper();
        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        JavaType jType = mapper.constructType(type);
        BeanDescription desc = mapper.getSerializationConfig()
                .introspect(jType);
        return desc;
    }

    public static Map<String, String> getFieldNameMapping(Class<?> type) {
        Map<String, String> fieldNames = new HashMap<>();
        BeanDescription desc = getBeanDescription(type);
        List<BeanPropertyDefinition> properties = desc.findProperties();
        for (BeanPropertyDefinition def : properties) {
            AnnotatedField field = def.getField();
            // fields with names where the JsonProperty name differs from the field name create zombie
            // entries here which we do not need.
            if (null != field) {
                Field annotated = field.getAnnotated();
                String fieldName = annotated.getName();
                String declaredName = def.getName();
                fieldNames.put(declaredName, fieldName);
            }
        }
        return fieldNames;
    }
}
