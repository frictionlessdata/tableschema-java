package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class ObjectField extends Field<JSONObject> {

    public ObjectField(String name) {
        super(name, FIELD_TYPE_OBJECT);
    }

    public ObjectField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_OBJECT, format, title, description, constraints);
    }

    public ObjectField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_OBJECT;
    }

    @Override
    JSONObject getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castObject(value, format, options);
    }
}
