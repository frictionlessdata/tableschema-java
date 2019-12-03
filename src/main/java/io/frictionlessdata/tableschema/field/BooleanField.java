package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class BooleanField extends Field<Boolean> {

    public BooleanField(String name) {
        super(name, FIELD_TYPE_BOOLEAN);
    }

    public BooleanField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_BOOLEAN, format, title, description, constraints);
    }

    public BooleanField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_BOOLEAN;
    }

    @Override
    Boolean getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castBoolean(value, format, options);
    }
}
