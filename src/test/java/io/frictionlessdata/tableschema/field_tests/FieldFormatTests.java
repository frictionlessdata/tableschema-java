package io.frictionlessdata.tableschema.field_tests;

import io.frictionlessdata.tableschema.field.BooleanField;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FieldFormatTests {

    @Test
    @DisplayName("format boolean values via default settings")
    void formatBooleanField() {
        BooleanField field = new BooleanField("bf");
        String val = field.formatValue(true, null, null);
        Assert.assertEquals("true", val);
        val = field.formatValue(false, null, null);
        Assert.assertEquals("false", val);
    }


    @Test
    @DisplayName("format boolean values with non-default true/false values")
    void formatBooleanField2() {
        Map<String, Object> options = new HashMap<>();
        options.put("trueValues", Arrays.asList("da", "ja", "oui"));
        options.put("falseValues", Arrays.asList("njet", "nein", "non"));

        BooleanField field = new BooleanField("bf");
        String val = field.formatValue(true, null, options);
        Assert.assertEquals("da", val);
        val = field.formatValue(false, null, options);
        Assert.assertEquals("njet", val);
    }
}
