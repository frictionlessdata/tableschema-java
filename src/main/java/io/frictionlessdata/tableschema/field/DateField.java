package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Map;

public class DateField extends Field<DateTime> {

    public DateField(String name) {
        super(name, FIELD_TYPE_DATE);
    }

    public DateField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_DATE, format, title, description, constraints);
    }

    public DateField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_DATE;
    }

    @Override
    DateTime getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castDate(format, value, options);
    }
}
