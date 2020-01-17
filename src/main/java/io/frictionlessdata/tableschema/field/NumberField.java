package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * [According to spec](http://frictionlessdata.io/specs/table-schema/index.html#number), a number field
 * consists of "a non-empty finite-length sequence of decimal digits".
 */

public class NumberField extends Field<Number> {
    private static final String NUMBER_OPTION_DECIMAL_CHAR = "decimalChar";
    private static final String NUMBER_OPTION_GROUP_CHAR = "groupChar";
    private static final String NUMBER_OPTION_BARE_NUMBER = "bareNumber";
    private static final String NUMBER_DEFAULT_DECIMAL_CHAR = ".";
    private static final String NUMBER_DEFAULT_GROUP_CHAR = "";

    private static final String REGEX_FLOAT = "([+-]?\\d*\\.?\\d*)";
    private static final String REGEX_INTEGER = "[+-]?\\d+";
    private static final String REGEX_BARE_NUMBER = "((^\\D*)|(\\D*$))";

    NumberField() {
        super();
    }

    public NumberField(String name) {
        super(name, FIELD_TYPE_NUMBER);
    }

    public NumberField(String name, String format, String title, String description,
                       URI rdfType, Map constraints, Map options){
        super(name, FIELD_TYPE_NUMBER, format, title, description, rdfType, constraints, options);
    }

    @Override
    public Number parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        String locValue = value.trim();
        try{
            if(options != null){
                if(options.containsKey(NUMBER_OPTION_DECIMAL_CHAR)){
                    locValue = locValue.replace((String)options.get(NUMBER_OPTION_DECIMAL_CHAR), NUMBER_DEFAULT_DECIMAL_CHAR);
                }

                if(options.containsKey(NUMBER_OPTION_GROUP_CHAR)){
                    locValue = locValue.replace((String)options.get(NUMBER_OPTION_GROUP_CHAR), NUMBER_DEFAULT_GROUP_CHAR);
                }

                if(options.containsKey(NUMBER_OPTION_BARE_NUMBER) && !(boolean)options.get(NUMBER_OPTION_BARE_NUMBER)){
                    locValue = locValue.replaceAll(REGEX_BARE_NUMBER, "");
                }
            }

            // Try to match integer pattern
            Pattern intergerPattern = Pattern.compile(REGEX_INTEGER);
            Matcher integerMatcher = intergerPattern.matcher(locValue);

            if(integerMatcher.matches()){
                return new BigInteger(locValue);
            }

            BigDecimal bd = new BigDecimal(locValue);
            return bd;
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }

    @Override
    public String formatValueAsString(Number value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        return value.toString();
    }


    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
