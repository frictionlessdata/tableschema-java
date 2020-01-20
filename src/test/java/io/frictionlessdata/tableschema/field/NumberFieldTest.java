package io.frictionlessdata.tableschema.field;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class NumberFieldTest {
    private static final Map<String,Object> germanOptions = new HashMap<>();
    private static final Map<String,Object> bareNumberOptions = new HashMap<>();

    @BeforeAll
    static void setUp() {
        germanOptions.put("decimalChar", ",");
        germanOptions.put("groupChar", ".");
        bareNumberOptions.put("bareNumber", false);
    }

    @Test
    @DisplayName("format number values via default settings")
    void formatNumberField() {
        NumberField field = new NumberField("test");
        String val = field.formatValueAsString(123123.123, null, null);
        Assert.assertEquals("123123.123", val);
    }

    @Test
    @DisplayName("format number values via non-default settings")
    void formatNumberField2() {
        NumberField field = new NumberField("test");
        String val = field.formatValueAsString(1234567890.123, null, germanOptions);
        Assert.assertEquals("1.234.567.890,123", val);
    }

    @Test
    @DisplayName("format float values via non-default settings")
    void formatNumberField4() {
        NumberField field = new NumberField("test");
        String val = field.formatValueAsString(123.678f, null, germanOptions);
        // value is not exact due to floating point
        Assert.assertEquals("123,6780014038086" , val);
    }

    @Test
    @DisplayName("format number values with barNumbers allowed")
    void formatNumberField3() {
        NumberField field = new NumberField("test");
        Number val = field.parseValue("123.234€", null, bareNumberOptions);
        Assert.assertEquals(123.234, val.doubleValue(), 0);
    }

    @Test
    @DisplayName("format NAN/INF double values")
    void formatNumberFieldNan() {
        NumberField field = new NumberField("test");
        String val = field.formatValueAsString(Double.NaN, null, null);
        Assert.assertEquals("NAN", val);
        val = field.formatValueAsString(Double.POSITIVE_INFINITY, null, null);
        Assert.assertEquals("INF", val);
        val = field.formatValueAsString(Double.NEGATIVE_INFINITY, null, null);
        Assert.assertEquals("-INF", val);
    }


    @Test
    @DisplayName("format NAN/INF float values")
    void formatNumberFieldNan2() {
        NumberField field = new NumberField("test");
        String val = field.formatValueAsString(Float.NaN, null, null);
        Assert.assertEquals("NAN", val);
        val = field.formatValueAsString(Float.POSITIVE_INFINITY, null, null);
        Assert.assertEquals("INF", val);
        val = field.formatValueAsString(Float.NEGATIVE_INFINITY, null, null);
        Assert.assertEquals("-INF", val);
    }

    @Test
    @DisplayName("format null value")
    void formatNumberField6() {
        NumberField field = new NumberField("test");
        String val = field.formatValueAsString(null, null, null);
        Assert.assertNull( val);
    }
}
