package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Map;

/**
 * [According to spec](http://frictionlessdata.io/specs/table-schema/index.html#number), a number field
 * consists of "a non-empty finite-length sequence of decimal digits".
 */
public class IntegerField extends Field<BigInteger> {

    IntegerField() {
        super();
    }

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
    public BigInteger parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return new BigInteger(value);
    }
}
