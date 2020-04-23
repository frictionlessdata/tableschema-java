package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

public class ObjectField extends Field<Map<String, Object>> {

    ObjectField() {
        super();
    }

    public ObjectField(String name) {
        super(name, FIELD_TYPE_OBJECT);
    }

    public ObjectField(String name, String format, String title, String description,
                       URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_OBJECT, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Map<String, Object> parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        return JsonUtil.getInstance().deserialize(value, new TypeReference<Map<String, Object>>(){});
    }


    @Override
    public String formatValueAsString(Map<String, Object> value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        return JsonUtil.getInstance().serialize(value);
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
