package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class YearMonthField extends Field<DateTime> {

    public YearMonthField(String name) {
        super(name, FIELD_TYPE_YEARMONTH);
    }

    public YearMonthField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_YEARMONTH, format, title, description, constraints);
    }

    public YearMonthField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_YEARMONTH;
    }

    @Override
    DateTime getCastValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return TypeInferrer.getInstance().castYearmonth(format, value, options);
    }
}
