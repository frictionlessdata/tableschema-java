package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BooleanField extends Field<Boolean> {
    List<String> trueValues = Arrays.asList("yes", "y", "true", "t", "1");
    List<String> falseValues = Arrays.asList("no", "n", "false", "f", "0");

    public BooleanField(String name) {
        super(name, FIELD_TYPE_BOOLEAN);
    }

    public BooleanField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_BOOLEAN, format, title, description, constraints);
    }

    public BooleanField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_BOOLEAN;
    }

    public void setTrueValues(List<String> newValues) {
        trueValues = newValues;
    }

    public void setFalseValues(List<String> newValues) {
        falseValues = newValues;
    }

    @Override
    Boolean parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (trueValues.contains(value.toLowerCase())){
            return true;

        }else if (falseValues.contains(value.toLowerCase())){
            return false;

        }else{
            throw new TypeInferringException();
        }
    }
}
