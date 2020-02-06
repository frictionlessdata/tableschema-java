package io.frictionlessdata.tableschema.field;

import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldFormatTests {

    @Test
    @DisplayName("format boolean values via default settings")
    void formatBooleanField() {
        BooleanField field = new BooleanField("bf");
        String val = field.formatValueAsString(true, null, null);
        Assert.assertEquals("true", val);
        val = field.formatValueAsString(false, null, null);
        Assert.assertEquals("false", val);
    }

    @Test
    @DisplayName("format boolean values with non-default true/false values")
    void formatBooleanField2() {
        Map<String, Object> options = new HashMap<>();
        options.put("trueValues", Arrays.asList("da", "ja", "oui"));
        options.put("falseValues", Arrays.asList("njet", "nein", "non"));

        BooleanField field = new BooleanField("bf");
        String val = field.formatValueAsString(true, null, options);
        Assert.assertEquals("da", val);
        val = field.formatValueAsString(false, null, options);
        Assert.assertEquals("njet", val);
    }

    @Test
    @DisplayName("parse boolean values with non-default true values")
    // conformance test for https://github.com/frictionlessdata/tableschema-java/issues/4
    void parseBooleanFieldWithCustomTrueValues() {
        BooleanField field = (BooleanField)BooleanField.fromJson("{'name': 'name', 'type': 'boolean', 'trueValues': ['agreed']}");

        // expect exception
        assertThrows(TypeInferringException.class, () -> field.parseValue("true", null, null));
        Boolean val = field.parseValue("agreed", null, null); // True
        Assertions.assertTrue(val);
    }

    @Test
    @DisplayName("parse boolean values with non-default false values")
        // conformance test for https://github.com/frictionlessdata/tableschema-java/issues/4
    void parseBooleanFieldWithCustomFalseValues() {
        BooleanField field = (BooleanField)BooleanField.fromJson("{'name': 'name', 'type': 'boolean', 'falseValues': ['declined']}");

        // expect exception
        assertThrows(TypeInferringException.class, () -> field.parseValue("false", null, null));
        Boolean val = field.parseValue("declined", null, null); // false
        Assertions.assertFalse(val);
    }

    @Test
    @DisplayName("format geopoint values via default settings")
    void formatGeopointField() {
        GeopointField field = new GeopointField("gpf");
        String val = field.formatValueAsString(new double[]{123.45, 56.789}, null, null);
        Assert.assertEquals("123.45,56.789", val);
        val = field.formatValueAsString(new double[]{123.45, 56.789}, Field.FIELD_FORMAT_DEFAULT, null);
        Assert.assertEquals("123.45,56.789", val);
        val = field.formatValueAsString(new double[]{123.45, 56.789}, Field.FIELD_FORMAT_ARRAY, null);
        Assert.assertEquals("[123.45,56.789]", val);
        val = field.formatValueAsString(new double[]{123.45, 56.789}, Field.FIELD_FORMAT_OBJECT, null);
        Assert.assertEquals("{\"lon\": 123.45, \"lat\":56.789}", val);
        val = field.formatValueAsString(new double[]{123.45, 56.789}, "invalid", null);
        Assert.assertNull(val);
    }


    @Test
    @DisplayName("parse time values")
    void parseTimeField() {
        TimeField field = (TimeField)TimeField.fromJson("{'name': 'name', 'type': 'time'}");

        // expect exception
        assertThrows(TypeInferringException.class, () -> field.parseValue("false", null, null));
        LocalTime val = field.parseValue("14:23:07", null, null); // false

        Assertions.assertEquals(14, val.getHour());
        Assertions.assertEquals(23, val.getMinute());
        Assertions.assertEquals(7, val.getSecond());

        String s = field.formatValueAsString(val, null, null);
        Assertions.assertEquals("14:23:07", s);
    }
}
