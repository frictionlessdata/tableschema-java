package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatetimeField extends Field<ZonedDateTime> {

    DatetimeField() {
        super();
    }

    public DatetimeField(String name) {
        super(name, FIELD_TYPE_DATETIME);
    }

    public DatetimeField(String name, String format, String title, String description,
                         URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_DATETIME, format, title, description, rdfType, constraints, options);
    }

    @Override
    public ZonedDateTime parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'][' ']HH:mm:ss[.SSS]['Z']");
            return LocalDateTime.parse(value, formatter).atZone(ZoneId.of("UTC"));
        }catch (Exception e){
            throw new TypeInferringException("DateTime field not in ISO 8601 format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        }
    }

    @Override
    public String formatValueAsString(ZonedDateTime value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
