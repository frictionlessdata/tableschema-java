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

public class DateTimeField extends Field<DateTime> {
    // ISO 8601 format of yyyy-MM-dd'T'HH:mm:ss.SSSZ in UTC time
    private static final String REGEX_DATETIME = "(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?";

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
    DateTime parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {

        Pattern pattern = Pattern.compile(REGEX_DATETIME);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            DateTime dt = formatter.parseDateTime(value);

            return dt;

        }else{
            throw new TypeInferringException();
        }
    }
}
