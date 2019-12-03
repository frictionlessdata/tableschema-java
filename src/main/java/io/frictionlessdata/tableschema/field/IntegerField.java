package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class IntegerField extends Field<Long> {

    public IntegerField(String name) {
        super(name, FIELD_TYPE_INTEGER);
    }

    public IntegerField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_INTEGER, format, title, description, constraints);
    }

    public IntegerField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_INTEGER;
    }

    @Override
    Long getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castInteger(value, format, options);
    }
}
