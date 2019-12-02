package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Map;

public class DateTimeField extends Field<DateTime> {

    public DateTimeField(String name) {
        super(name, FIELD_TYPE_DATETIME);
    }

    public DateTimeField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_DATETIME, format, title, description, constraints);
    }

    public DateTimeField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_DATETIME;
    }

    @Override
    DateTime getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castDatetime(format, value, options);
    }
}
