package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.json.JSONObject;

import java.net.URI;
import java.util.*;

public class BooleanField extends Field<Boolean> {
    List<String> trueValues = Arrays.asList("true", "yes", "y", "t", "1");
    List<String> falseValues = Arrays.asList("false", "no", "n", "f", "0");


    BooleanField() {
        super();
    }

    public BooleanField(String name) {
        super(name, FIELD_TYPE_BOOLEAN);
    }

    public BooleanField(String name, String format, String title, String description,
                        URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_BOOLEAN, format, title, description, rdfType, constraints, options);
    }

    public void setTrueValues(List<String> newValues) {
        trueValues = newValues;
    }

    public void setFalseValues(List<String> newValues) {
        falseValues = newValues;
    }

    @Override
    public Boolean parseValue(String value, String format, Map<String, Object> options)
            throws InvalidCastException, ConstraintsException {
        if (null != options) {
            if (options.containsKey("trueValues")) {
                trueValues = new ArrayList<>((Collection) options.get("trueValues"));
            }
            if (options.containsKey("falseValues")) {
                falseValues = new ArrayList<>((Collection) options.get("falseValues"));
            }
        }

        if (trueValues.contains(value.toLowerCase())){
            return true;

        }else if (falseValues.contains(value.toLowerCase())){
            return false;

        }else{
            throw new TypeInferringException();
        }
    }

    @Override
    public String formatValue(Boolean value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        String trueValue = trueValues.get(0);
        String falseValue = falseValues.get(0);
        if (null != options) {
            if (options.containsKey("trueValues")) {
                trueValue = new ArrayList<String>((Collection) options.get("trueValues")).iterator().next();
            }
            if (options.containsKey("falseValues")) {
                falseValue = new ArrayList<String>((Collection) options.get("falseValues")).iterator().next();
            }
        }
        return (value) ? trueValue : falseValue;
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

}
