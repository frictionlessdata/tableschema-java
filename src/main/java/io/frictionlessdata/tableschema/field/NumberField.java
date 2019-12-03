package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.TypeInferrer;
import io.frictionlessdata.tableschema.exceptions.ConstraintsException;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberField extends Field<Number> {
    private static final String NUMBER_OPTION_DECIMAL_CHAR = "decimalChar";
    private static final String NUMBER_OPTION_GROUP_CHAR = "groupChar";
    private static final String NUMBER_OPTION_BARE_NUMBER = "bareNumber";
    private static final String NUMBER_DEFAULT_DECIMAL_CHAR = ".";
    private static final String NUMBER_DEFAULT_GROUP_CHAR = "";

    private static final String REGEX_FLOAT = "([+-]?\\d*\\.?\\d*)";
    private static final String REGEX_INTEGER = "[+-]?\\d+";
    private static final String REGEX_BARE_NUMBER = "((^\\D*)|(\\D*$))";

    public NumberField(String name) {
        super(name, FIELD_TYPE_NUMBER);
    }

    public NumberField(String name, String format, String title, String description, Map constraints) {
        super(name, FIELD_TYPE_NUMBER, format, title, description, constraints);
    }

    public NumberField(JSONObject field) {
        super(field);
        type = FIELD_TYPE_NUMBER;
    }

    @Override
    public Number parseValue(String value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        try{
            if(options != null){
                if(options.containsKey(NUMBER_OPTION_DECIMAL_CHAR)){
                    value = value.replace((String)options.get(NUMBER_OPTION_DECIMAL_CHAR), NUMBER_DEFAULT_DECIMAL_CHAR);
                }

                if(options.containsKey(NUMBER_OPTION_GROUP_CHAR)){
                    value = value.replace((String)options.get(NUMBER_OPTION_GROUP_CHAR), NUMBER_DEFAULT_GROUP_CHAR);
                }

                if(options.containsKey(NUMBER_OPTION_BARE_NUMBER) && !(boolean)options.get(NUMBER_OPTION_BARE_NUMBER)){
                    value = value.replaceAll(REGEX_BARE_NUMBER, "");
                }
            }

            // Try to match integer pattern
            Pattern intergerPattern = Pattern.compile(REGEX_INTEGER);
            Matcher integerMatcher = intergerPattern.matcher(value);

            if(integerMatcher.matches()){
                return Integer.parseInt(value);
            }

            // Try to match float pattern
            Pattern floatPattern = Pattern.compile(REGEX_FLOAT);
            Matcher floatMatcher = floatPattern.matcher(value);

            if(floatMatcher.matches()){
                return Float.parseFloat(value);
            }

            // The value failed to match neither the Float or the Integer value.
            // Throw exception.
            throw new TypeInferringException();

        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
}
