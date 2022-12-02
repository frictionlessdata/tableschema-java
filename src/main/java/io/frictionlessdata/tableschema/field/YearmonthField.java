package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearmonthField extends Field<YearMonth> {
    // yyyy-MM
    private static final String REGEX_YEARMONTH = "([0-9]{4})-(1[0-2]|0[1-9])";

    YearmonthField() {
        super();
    }

    public YearmonthField(String name) {
        super(name, FIELD_TYPE_YEARMONTH);
    }

    public YearmonthField(String name, String format, String title, String description,
                          URI rdfType, Map<String, Object> constraints, Map<String, Object> options) {
        super(name, FIELD_TYPE_YEARMONTH, format, title, description, rdfType, constraints, options);
    }

    @Override
    public YearMonth parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {
        Pattern pattern = Pattern.compile(REGEX_YEARMONTH);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            TemporalAccessor dt = formatter.parse(value);

            return YearMonth.from(dt);
        }else{
            throw new TypeInferringException();
        }
    }

    @Override
    public String formatValueAsString(YearMonth value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.toString();
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    YearMonth checkMinimumContraintViolated(YearMonth value) {
        YearMonth minDate = (YearMonth)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
        if(value.isBefore(minDate)){
            return minDate;
        }
        return null;
    }
}
