package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class ArrayField extends Field<JSONArray> {

    ArrayField() {
        super();
    }

    public ArrayField(String name) {
        super(name, FIELD_TYPE_ARRAY);
    }

    public ArrayField(String name, String format, String title, String description, Map constraints, Map options){
        super(name, FIELD_TYPE_ARRAY, format, title, description, constraints, options);
    }

    @Override
    public JSONArray parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        return new JSONArray(value);
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
