package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.Year;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YearField extends Field<Year> {
    // yyyy
    private static final String REGEX_YEAR = "([0-9]{4})";

    YearField() {
        super();
    }

    public YearField(String name) {
        super(name, FIELD_TYPE_YEAR);
    }

    public YearField(String name, String format, String title, String description,
                     URI rdfType, Map<String, Object> constraints, Map<String, Object> options){
        super(name, FIELD_TYPE_YEAR, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Year parseValue(String value, String format, Map<String, Object> options) throws TypeInferringException {
        Pattern pattern = Pattern.compile(REGEX_YEAR);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            return Year.parse(value);

        }else{
            throw new TypeInferringException();
        }
    }

    @Override
    public Object formatValueForJson(Year value) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.getValue();
    }

    @Override
    public String formatValueAsString(Year value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.toString();
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
    Year checkMinimumContraintViolated(Year value) {
        int minYear = (int)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
        if(value.isBefore(Year.of(minYear))) {
            return Year.of(minYear);
        }
        return null;
    }


}
