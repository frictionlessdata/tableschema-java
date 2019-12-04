package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearmonthField extends Field<DateTime> {
    // yyyy-MM
    private static final String REGEX_YEARMONTH = "([0-9]{4})-(1[0-2]|0[1-9])";

    YearmonthField() {
        super();
    }

    public YearmonthField(String name) {
        super(name, FIELD_TYPE_YEARMONTH);
    }

    public YearmonthField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_YEARMONTH, format, title, description, constraints);
    }

    public YearmonthField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_YEARMONTH;
    }

    @Override
    public DateTime parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        Pattern pattern = Pattern.compile(REGEX_YEARMONTH);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM");
            DateTime dt = formatter.parseDateTime(value);

            return dt;

        }else{
            throw new TypeInferringException();
        }
    }
}
