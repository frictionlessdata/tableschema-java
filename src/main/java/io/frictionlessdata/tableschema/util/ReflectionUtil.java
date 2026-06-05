package io.frictionlessdata.tableschema.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.introspect.AnnotatedField;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.dataformat.csv.CsvMapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionUtil {

    public static BeanDescription getBeanDescription(Class<?> type) {
        CsvMapper mapper = CsvMapper.builder()
                .changeDefaultVisibility(v -> v.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build();

        JavaType jType = mapper.constructType(type);
        var config = mapper.serializationConfig();
        var introspector = config.classIntrospectorInstance().forOperation(config);

        return introspector.introspectForSerialization(
                jType,
                introspector.introspectClassAnnotations(jType)
        );
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
