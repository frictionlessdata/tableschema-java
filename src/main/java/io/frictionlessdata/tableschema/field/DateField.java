package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;

public class DateField extends Field<LocalDate> {

    private static String patternFormat = "yyyy-MM-dd";
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patternFormat);

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

        if(value.length()>patternFormat.length() || value.length() < "y-M-d".length()){
            throw new TypeInferringException();
        }

       try{
            TemporalAccessor dt = formatter.parse(value);
            return LocalDate.from(dt);
        }
       catch (Exception e){
            throw new TypeInferringException();
        }
    }

    @Override
    public String formatValueAsString(LocalDate value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.format(DateTimeFormatter.ofPattern(patternFormat));
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
