package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Locale;
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

    private static final String REGEX_INTEGER = "[+-]?\\d+";
    private static final String REGEX_BARE_NUMBER = "((^\\D*)|(\\D*$))";

    private static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
    static {
        numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        numberFormat.setGroupingUsed(false);
    }

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
            Pattern intPattern = Pattern.compile(REGEX_INTEGER);
            Matcher integerMatcher = intPattern.matcher(locValue);

            if(integerMatcher.matches()){
                return new BigInteger(locValue);
            }

            // BigDecimal doesn't have NAN, INF...
            if (locValue.equalsIgnoreCase("NaN")) {
                return Double.NaN;
            } else if (locValue.equalsIgnoreCase("INF")) {
                return Double.POSITIVE_INFINITY;
            } else if (locValue.equalsIgnoreCase("-INF")) {
                return Double.NEGATIVE_INFINITY;
            }
            return new BigDecimal(locValue);
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    public Object formatValueForJson(Number value) throws InvalidCastException, ConstraintsException {
        return formatValueAsString(value, null, options);
    }

    @Override
    public String formatValueAsString(Number value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
        if (null == value)
            return null;
        if (value instanceof Double) {
            Double locVal = (Double)value;
            if (locVal.equals(Double.NaN)) {
                return "NAN";
            } else if (locVal.equals(Double.POSITIVE_INFINITY)) {
                return "INF";
            } else if (locVal.equals(Double.NEGATIVE_INFINITY)) {
                return "-INF";
            } else {
                return formatNumber(numberFormat.format(locVal), options);
            }
        } else if (value instanceof Float) {
            Float locVal = (Float)value;
            if (locVal.equals(Float.NaN)) {
                return "NAN";
            } else if (locVal.equals(Float.POSITIVE_INFINITY)) {
                return "INF";
            } else if (locVal.equals(Float.NEGATIVE_INFINITY)) {
                return "-INF";
            } else {
                return formatNumber(numberFormat.format(locVal), options);
            }
        }else if (value instanceof BigInteger) {
            return formatNumber(value.toString(), options);
        } else if (value instanceof BigDecimal) {
            return formatNumber(((BigDecimal)value).toPlainString(), options);
        }
        return value.toString();
    }

    private static String formatNumber(String value, Map<String, Object> options) {
        String decSeparator = ".";
        String locValue = value;
        if(options != null){
            if (options.containsKey(NUMBER_OPTION_DECIMAL_CHAR)){
                decSeparator = ((String)options.get(NUMBER_OPTION_DECIMAL_CHAR));
                locValue = locValue.replace(".", decSeparator);
            }

            if(options.containsKey(NUMBER_OPTION_GROUP_CHAR)){
                String groupSeparator = ((String)options.get(NUMBER_OPTION_GROUP_CHAR));
                String intPart = intPart(locValue, decSeparator);
                String remainder = locValue.replace(intPart, "");
                locValue = insertThousandsGroupSeparator(intPart(locValue, decSeparator), groupSeparator)+remainder;
            }
        }
        return locValue;
    }

    private static String intPart(String input, String decimalSep) {
        int pos = input.indexOf(decimalSep);
        if (pos == -1)
            return input;
        return input.substring(0, pos);
    }

    private static String insertThousandsGroupSeparator(String input, String groupSeparator) {
        int length = input.length();
        if (length <= 3)
            return input;
        String locString = input.substring(0, length -3);
        String remainder = input.replace(locString, "");
        return insertThousandsGroupSeparator(locString, groupSeparator)+groupSeparator+remainder;
    }

    @Override
    public String parseFormat(String value, Map<String, Object> options) {
        return "default";
    }
}
