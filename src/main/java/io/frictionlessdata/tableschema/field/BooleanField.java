package io.frictionlessdata.tableschema.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.net.URI;
import java.util.*;

public class BooleanField extends Field<Boolean> {
    @JsonIgnore
    private static final List<String> defaultTrueValues = Arrays.asList("true", "True", "TRUE", "1");

    @JsonIgnore
    private static final List<String> defaultFalseValues = Arrays.asList("false", "False", "FALSE", "0");

    private List<String> trueValues = null;
    private List<String> falseValues = null;


    BooleanField() {
        super();
    }

    public BooleanField(String name) {
        super(name, FIELD_TYPE_BOOLEAN);
    }

    public BooleanField(String name, String format, String title, String description,
                        URI rdfType, Map<String, Object> constraints, Map<String, Object> options){
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
            throws TypeInferringException {
        if (null != options) {
            if (options.containsKey("trueValues")) {
                trueValues = new ArrayList<>((Collection) options.get("trueValues"));
            }
            if (options.containsKey("falseValues")) {
                falseValues = new ArrayList<>((Collection) options.get("falseValues"));
            }
        }

        if (_getActualTrueValues().contains(value)){
            return true;

        }else if (_getActualFalseValues().contains(value)){
            return false;

        }else{
            throw new TypeInferringException("Value "+value+" not in 'trueValues' or 'falseValues'");
        }
    }

    @Override
    public String formatValueAsString(Boolean value) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        return (value) ? _getActualTrueValues().get(0) : _getActualFalseValues().get(0);
    }


    @Override
    public String formatValueAsString(Boolean value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        String trueValue = _getActualTrueValues().get(0);
        String falseValue = _getActualFalseValues().get(0);
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
    String formatObjectValueAsString(Object value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }

    @Override
    Boolean checkMinimumContraintViolated(Boolean value) {
        return null;
    }

    public static Field fromJson (String json) {
    	return Field.fromJson(json);
    }

    public List<String> getTrueValues() {
        return trueValues;
    }

    public List<String> getFalseValues() {
        return falseValues;
    }



    @JsonIgnore
    private List<String> _getActualTrueValues() {
        if ((null == trueValues) || (trueValues.isEmpty()))
            return defaultTrueValues;
        return trueValues;
    }

    @JsonIgnore
    private List<String> _getActualFalseValues() {
        if ((null == falseValues) || (falseValues.isEmpty()))
            return defaultFalseValues;
        return falseValues;
    }
}
