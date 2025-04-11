package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

public class DurationField extends Field<Duration> {

    DurationField() {
        super();
    }

    public DurationField(String name) {
        super(name, FIELD_TYPE_DURATION);
    }

    public DurationField(String name, String format, String title, String description,
                         URI rdfType, Map<String, Object> constraints, Map<String, Object> options, String example){
        super(name, FIELD_TYPE_DURATION, format, title, description, rdfType, constraints, options, example);
    }

    @Override
    public Duration parseValue(String value, String format, Map<String, Object> options)
            throws TypeInferringException {
        try{
            return Duration.parse(value);
        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    @Override
    public String formatValueAsString(Duration value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
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
    Duration checkMinimumConstraintViolated(Duration value) {
        Duration minDuration = (Duration)this.constraints.get(CONSTRAINT_KEY_MINIMUM);
        if(value.compareTo(minDuration) < 0){
            return minDuration;
        }
        return null;
    }
}
