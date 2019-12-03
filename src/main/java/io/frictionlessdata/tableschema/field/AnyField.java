package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class AnyField extends Field<Object> {

    public AnyField(String name) {
        super(name, FIELD_TYPE_ANY);
    }

    public AnyField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_ANY, format, title, description, constraints);
    }

    public AnyField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_ANY;
    }

    @Override
    Object getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value;
    }
}
