package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.ConstraintsException;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Map;

/**
 * [According to spec](http://frictionlessdata.io/specs/table-schema/index.html#number), a number field
 * consists of "a non-empty finite-length sequence of decimal digits".
 */
public class IntegerField extends Field<BigInteger> {

    IntegerField() {
        super();
    }

    public IntegerField(String name) {
        super(name, FIELD_TYPE_INTEGER);
    }

    public IntegerField(String name, String format, String title, String description,
                        URI rdfType, Map<String, Object> constraints, Map<String, Object> options) {
        super(name, FIELD_TYPE_INTEGER, format, title, description, rdfType, constraints, options);
    }

    @Override
    public BigInteger parseValue(String value, String format, Map<String, Object> options) throws TypeInferringException {
        try {
            return new BigInteger(value.trim());
        } catch (Exception ex) {
            throw new TypeInferringException(ex);
        }
    }

    @Override
    public String formatValueAsString(BigInteger value, String format, Map<String, Object> options) throws InvalidCastException, ConstraintsException {
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
    BigInteger checkMinimumContraintViolated(BigInteger value) {
        BigInteger minNumber = new BigInteger(this.constraints.get(CONSTRAINT_KEY_MINIMUM).toString());
        if( new BigInteger(value.toString()).compareTo(minNumber) < 0 ) {
            return minNumber;
        }
        return null;
    }

    BigInteger checkMinimumContraintViolated(Integer value) {
        BigInteger minNumber = new BigInteger(this.constraints.get(CONSTRAINT_KEY_MINIMUM).toString());
        if( new BigInteger(value.toString()).compareTo(minNumber) < 0 ) {
            return minNumber;
        }
        return null;
    }
}
