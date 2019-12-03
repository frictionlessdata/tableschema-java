package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeField extends Field<DateTime> {
    // An ISO8601 time string e.g. HH:mm:ss
    private static final String REGEX_TIME = "(2[0-3]|[01]?[0-9]):?([0-5]?[0-9]):?([0-5]?[0-9])";

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
        Pattern pattern = Pattern.compile(REGEX_TIME);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
            DateTime dt = formatter.parseDateTime(value);

            return dt;

        }else{
            throw new TypeInferringException();
        }
    }
}
