package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearField extends Field<Integer> {
    // yyyy
    private static final String REGEX_YEAR = "([0-9]{4})";

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
        Pattern pattern = Pattern.compile(REGEX_YEAR);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            int year = Integer.parseInt(value);
            return year;

        }else{
            throw new TypeInferringException();
        }
    }
}
