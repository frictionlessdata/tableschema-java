package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatetimeField extends Field<ZonedDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    // ISO 8601 format of yyyy-MM-dd'T'HH:mm:ss.SSSZ in UTC time
    private static final String REGEX_DATETIME
            = "(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?";

    DatetimeField() {
        super();
    }

    public DatetimeField(String name) {
        super(name, FIELD_TYPE_DATETIME);
    }

    public DatetimeField(String name, String format, String title, String description,
                         URI rdfType, Map<String, Object> constraints, Map<String, Object> options, String example){
        super(name, FIELD_TYPE_DATETIME, format, title, description, rdfType, constraints, options, example);
    }

    @Override
    public ZonedDateTime parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {

        Pattern pattern = Pattern.compile(REGEX_DATETIME);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            TemporalAccessor dt = FORMATTER.parse(value);

            return ZonedDateTime.from(dt);
        }else{
            throw new TypeInferringException("DateTime field value [" + value + "] not in ISO 8601 format yyyy-MM-dd'T'HH:mm:ss[.SSS]Z");
        }
    }

    @Override
    public String formatValueAsString(ZonedDateTime value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.format(FORMATTER);
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
    ZonedDateTime checkMinimumContraintViolated(ZonedDateTime value) {
        ZonedDateTime minTime = (ZonedDateTime)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
        if(value.isBefore(minTime)){
            return minTime;
        }
        return null;
    }


}
