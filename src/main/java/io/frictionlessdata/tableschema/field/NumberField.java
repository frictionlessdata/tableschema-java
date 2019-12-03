package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.util.Map;

public class NumberField extends Field<Number> {

    public NumberField(String name) {
        super(name, FIELD_TYPE_NUMBER);
    }

    public NumberField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_NUMBER, format, title, description, constraints);
    }

    public NumberField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_NUMBER;
    }

    @Override
    Number getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castNumber(value, format, options);
    }
}
