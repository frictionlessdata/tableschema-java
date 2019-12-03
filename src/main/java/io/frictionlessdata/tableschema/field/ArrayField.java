package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class ArrayField extends Field<JSONArray> {

    public ArrayField(String name) {
        super(name, FIELD_TYPE_ARRAY);
    }

    public ArrayField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_ARRAY, format, title, description, constraints);
    }

    public ArrayField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_ARRAY;
    }

    @Override
    JSONArray getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castArray(value, format, options);
    }
}
