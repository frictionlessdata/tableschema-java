package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateField extends Field<LocalDate> {
    // ISO8601 format yyyy-MM-dd
    private static final String REGEX_DATE = "([0-9]{4})-(1[0-2]|0[1-9])-(3[0-1]|0[1-9]|[1-2][0-9])";

    DateField() {
        super();
    }

    public DateField(String name) {
        super(name, FIELD_TYPE_DATE);
    }

    public DateField(String name, String format, String title, String description,
                     URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_DATE, format, title, description, rdfType, constraints, options);
    }

    @Override
    public LocalDate parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {

        Pattern pattern = Pattern.compile(REGEX_DATE);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            TemporalAccessor dt = formatter.parse(value);

            return LocalDate.from(dt);

        }else{
            throw new TypeInferringException();
        }
    }

    @Override
    public String formatValue(LocalDate value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
