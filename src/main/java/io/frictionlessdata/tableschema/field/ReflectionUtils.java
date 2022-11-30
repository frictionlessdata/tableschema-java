package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionUtils {

    public static Map<String, String> getFieldNameMapping(ObjectMapper mapper, Class type) {
        Map<String, String> fieldNames = new HashMap<>();
        JavaType jType = mapper.constructType(type);
        BeanDescription desc = mapper.getSerializationConfig()
                .introspect(jType);
        List<BeanPropertyDefinition> properties = desc.findProperties();
        for (BeanPropertyDefinition def : properties) {
            AnnotatedField field = def.getField();
            Field annotated = field.getAnnotated();
            String fieldName = annotated.getName();
            //String fieldName = def.getInternalName();
            String declaredName = def.getName();
            fieldNames.put(declaredName, fieldName);
        }
        return fieldNames;
    }
}
