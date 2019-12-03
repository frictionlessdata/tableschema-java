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

public class DateField extends Field<DateTime> {
    // ISO8601 format yyyy-MM-dd
    private static final String REGEX_DATE = "([0-9]{4})-(1[0-2]|0[1-9])-(3[0-1]|0[1-9]|[1-2][0-9])";

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
    public DateTime parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {

        Pattern pattern = Pattern.compile(REGEX_DATE);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            DateTime dt = formatter.parseDateTime(value);

            return dt;

        }else{
            throw new TypeInferringException();
        }
    }
}
