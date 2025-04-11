package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.core.type.TypeReference;
import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.JsonParsingException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.util.JsonUtil;

import java.net.URI;
import java.util.Map;

public class ObjectField extends Field<Map<String, Object>> {

    ObjectField() {
        super();
    }

    public ObjectField(String name) {
        super(name, FIELD_TYPE_OBJECT);
    }

    public ObjectField(String name, String format, String title, String description,
                       URI rdfType, Map<String, Object> constraints, Map<String, Object> options, String example){
        super(name, FIELD_TYPE_OBJECT, format, title, description, rdfType, constraints, options,   example);
    }

    @Override
    public Map<String, Object> parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {
        try {
            return JsonUtil.getInstance().deserialize(value, new TypeReference<Map<String, Object>>() {});
        } catch (JsonParsingException ex) {
            throw new TypeInferringException(ex);
        }
    }

    @Override
    public String formatValueAsString(Map<String, Object> value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return JsonUtil.getInstance().serialize(value, false).replaceAll("[\n\r]", " ");
    }

    @Override
    String formatObjectValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    Map<String, Object> checkMinimumConstraintViolated(Map<String, Object> value) {
        return null;
    }
}
