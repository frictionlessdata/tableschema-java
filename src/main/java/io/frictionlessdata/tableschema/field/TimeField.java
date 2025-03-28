package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeField extends Field<LocalTime> {
    // An ISO8601 time string e.g. HH:mm:ss
    private static final String REGEX_TIME = "(2[0-3]|[01]?[0-9]):?([0-5]?[0-9]):?([0-5]?[0-9])";

    TimeField() {
        super();
    }

    public TimeField(String name) {
        super(name, FIELD_TYPE_TIME);
    }

    public TimeField(String name, String format, String title, String description,
                     URI rdfType, Map<String, Object> constraints, Map<String, Object> options, String example){
        super(name, FIELD_TYPE_TIME, format, title, description, rdfType, constraints, options, example);
    }

    @Override
    public LocalTime parseValue(String value, String format, Map<String, Object> options) throws TypeInferringException {
        Pattern pattern = Pattern.compile(REGEX_TIME);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            return LocalTime.parse(value);

        }else{
            throw new TypeInferringException();
        }
    }

    @Override
    public Object formatValueForJson(LocalTime value) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    @Override
    public String formatValueAsString(LocalTime value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    @Override
    String formatObjectValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    LocalTime checkMinimumContraintViolated(LocalTime value) {
        LocalTime minTime = (LocalTime)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
        if(value.isBefore(minTime)){
           return minTime;
        }
        return null;
    }
}
