package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Map;

public class YearField extends Field<Integer> {

    public YearField(String name) {
        super(name, FIELD_TYPE_YEAR);
    }

    public YearField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_YEAR, format, title, description, constraints);
    }

    public YearField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_YEAR;
    }

    @Override
    Integer getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castYear(value, format, options);
    }
}
