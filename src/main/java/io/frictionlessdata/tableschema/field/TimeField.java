package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Map;

public class TimeField extends Field<DateTime> {

    public TimeField(String name) {
        super(name, FIELD_TYPE_TIME);
    }

    public TimeField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_TIME, format, title, description, constraints);
    }

    public TimeField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_TIME;
    }

    @Override
    DateTime getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castTime(format, value, options);
    }
}
