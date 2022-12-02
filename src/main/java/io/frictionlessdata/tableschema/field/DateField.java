package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
                     URI rdfType, Map<String, Object> constraints, Map<String, Object> options){
        super(name, FIELD_TYPE_DATE, format, title, description, rdfType, constraints, options);
    }

    @Override
    public LocalDate parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {

        Pattern pattern = Pattern.compile(REGEX_DATE);
        Matcher matcher = pattern.matcher(value);

        if(matcher.matches()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            TemporalAccessor dt = formatter.parse(value);

            return LocalDate.from(dt);
        } else {
            if (((!format.equals(Field.FIELD_FORMAT_DEFAULT))
                    && (!format.equals(Field.FIELD_FORMAT_ANY)))) {
                /* Nasty Python-specific time patterns:
                    <PATTERN>: date/time values in this field can be parsed according to
                    <PATTERN>. <PATTERN> MUST follow the syntax of standard Python / C
                     strptime (That is, values in the this field should be parsable
                    by Python / C standard strptime using <PATTERN>). Example for "format": "%d/%m/%y" which
                    would correspond to dates like: 30/11/14
                 */
                String regex = format
                        .replaceAll("%d", "dd")
                        .replaceAll("%m", "MM")
                        .replaceAll("%y", "yy");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(regex);
                try {
                    return LocalDate.from(formatter.parse(value));
                } catch (DateTimeParseException ex) {
                    regex = format
                            .replaceAll("%d", "dd")
                            .replaceAll("%m", "MM")
                            .replaceAll("%y", "yyyy");

                    formatter = DateTimeFormatter.ofPattern(regex);
                    return LocalDate.from(formatter.parse(value));
                }
            }
            throw new TypeInferringException();
        }
    }

    @Override
    public Object formatValueForJson(LocalDate value) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.toString();
    }

    @Override
    public String formatValueAsString(LocalDate value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    LocalDate checkMinimumContraintViolated(LocalDate value) {
        LocalDate minDate = (LocalDate)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
        if(value.isBefore(minDate)){
            return minDate;
        }
        return null;
    }


}
