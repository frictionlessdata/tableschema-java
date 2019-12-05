package io.frictionlessdata.tableschema.field_tests;

import io.frictionlessdata.tableschema.Schema;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.field.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * 
 */
class FieldFormatParsingTest {


    // any fields have no format options
    @Test
    void testFieldParseFormatFromAny() throws Exception{
        AnyField field = new AnyField("test");
        String format = field.parseFormat("2008", null);
        Assertions.assertEquals("default", format);
    }

    // boolean fields have no format options
    @Test
    @DisplayName("Boolean Field returns default format")
    void testFieldParseFormatFromBoolean() throws Exception {
        BooleanField field = new BooleanField("test");

        String format = field.parseFormat("f", null);
        Assertions.assertEquals("default", format);
    }

    // geopoint field, test default format parsing
    @Test
    @DisplayName("Geopoint Field returns default format")
    void testFieldParseFormatFromGeopointDefault() throws Exception{
        GeopointField field = new GeopointField("test");
        String format = field.parseFormat("12,21", null);
        Assertions.assertEquals("default", format);
    }

    // geopoint field, test array format parsing
    @Test
    @DisplayName("Geopoint Field returns array format")
    void testFieldParseFormatFromGeopointArray() throws Exception{
        GeopointField field = new GeopointField("test");
        String format = field.parseFormat("[45,32]", null);
        Assertions.assertEquals("array", format);
    }

    // geopoint field, test oject format parsing
    @Test
    @DisplayName("Geopoint Field returns object format")
    void testFieldParseFormatFromGeopointObject() throws Exception{
        GeopointField field = new GeopointField("test");
        String format = field.parseFormat("{\"lon\": 67, \"lat\": 19}", null);
        Assertions.assertEquals("object", format);
    }

    // integer fields have no format options
    @Test
    @DisplayName("Integer Field returns default format")
    void testFieldParseFormatFromInteger() throws Exception{
        IntegerField field = new IntegerField("test");
        String format = field.parseFormat("123", null);
        Assertions.assertEquals("default", format);
    }

    // number fields have no format options
    @Test
    @DisplayName("Number Field returns default format")
    void testFieldParseFormatFromNumber() throws Exception{
        NumberField field = new NumberField("test");
        String format = field.parseFormat("123.01", null);
        Assertions.assertEquals("default", format);
    }

    // duration fields have no format options
    @Test
    @DisplayName("Duration Field returns default format")
    void testFieldParseFormatFromDuration() throws Exception{
        DurationField field = new DurationField("test");
        String format = field.parseFormat("P2DT3H4M", null);
        Assertions.assertEquals("default", format);
    }

    @Test
    @DisplayName("String Field returns default format")
    void testFieldParseFormatFromString() throws Exception{
        StringField field = new StringField("test");
        String format = field.parseFormat("John Doe", null);
        Assertions.assertEquals(Field.FIELD_FORMAT_DEFAULT, format);
    }

    @Test
    @DisplayName("String Field returns UUID format")
    void testFieldParseUUIDFormatFromString() throws Exception{
        StringField field = new StringField("test");

        String format = field.parseFormat("6aed4d5f-de7c-4233-ab70-b64d054b11f3", null);
        Assertions.assertEquals(Field.FIELD_FORMAT_UUID, format);

        String nilFormat = field.parseFormat("00000000-0000-0000-0000-000000000000", null);
        Assertions.assertEquals(Field.FIELD_FORMAT_UUID, nilFormat);
    }

    @Test
    @DisplayName("String Field returns URI format")
    void testFieldParseURIFormatFromString() throws Exception{
        StringField field = new StringField("test");

        String format = field.parseFormat("https://github.com", null);
        Assertions.assertEquals(Field.FIELD_FORMAT_URI, format);

        String urnFormat = field.parseFormat("urn:oasis:names:specification:docbook:dtd:xml:4.1.2", null);
        Assertions.assertEquals(Field.FIELD_FORMAT_URI, urnFormat);

        String mailFormat = field.parseFormat("mailto:John.Doe@example.com", null);
        Assertions.assertEquals(Field.FIELD_FORMAT_URI, mailFormat);

        String invalidFormat = field.parseFormat("london", null);
        Assertions.assertNotEquals(Field.FIELD_FORMAT_URI, invalidFormat);
    }

    // not gonna test all the possible edge cases for e-mail addresses,
    // we just rely on the Apache Validator wisdom
    @Test
    @DisplayName("String Field returns email format")
    void testFieldParseEmailFormatFromString() throws Exception{
        StringField field = new StringField("test");

        String format = field.parseFormat("john.smith@somewhere.com", null);
        Assertions.assertEquals(Field.FIELD_FORMAT_EMAIL, format);

        String invalidFormat = field.parseFormat("john.smith_somewhere.com", null);
        Assertions.assertNotEquals(Field.FIELD_FORMAT_EMAIL, invalidFormat);
    }


    @DisplayName("Geojson Field returns Geojson format")
    @Test
    void testFieldParseFormatFromValidGeojson() throws Exception{
        GeojsonField field = new GeojsonField("test");
        String val = "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "  {\n" +
                "    \"type\": \"Feature\",\n" +
                "    \"properties\": {\n" +
                "      \"name\": \"codecentric AG\",\n" +
                "      \"address\": \"Hochstr. 11\",\n" +
                "      \"marker-color\": \"#008800\",\n" +
                "      \"marker-symbol\": \"commercial\"\n" +
                "    },\n" +
                "    \"geometry\": {\n" +
                "      \"type\": \"Point\",\n" +
                "      \"coordinates\": [7.0069, 51.1623]\n" +
                "    }\n" +
                "  }\n" +
                "]\n" +
                "}";

        String format = field.parseFormat( val, null);
        Assertions.assertEquals(Field.FIELD_TYPE_GEOJSON, format);
    }

    /*
    @Test
    void testFieldParseFormatFromValidTopojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_TOPOJSON, null, null, null, null);

        JSONObject val = field.castValue("{\n" +
            "  \"type\": \"Topology\",\n" +
            "  \"transform\": {\n" +
            "    \"scale\": [0.036003600360036005, 0.017361589674592462],\n" +
            "    \"translate\": [-180, -89.99892578124998]\n" +
            "  },\n" +
            "  \"objects\": {\n" +
            "    \"aruba\": {\n" +
            "      \"type\": \"Polygon\",\n" +
            "      \"arcs\": [[0]],\n" +
            "      \"id\": 533\n" +
            "    }\n" +
            "  },\n" +
            "  \"arcs\": [\n" +
            "    [[3058, 5901], [0, -2], [-2, 1], [-1, 3], [-2, 3], [0, 3], [1, 1], [1, -3], [2, -5], [1, -1]]\n" +
            "  ]\n" +
            "}");
        
        Assertions.assertEquals("Topology", val.getString("type"));
        Assertions.assertEquals(0.036003600360036005, val.getJSONObject("transform").getJSONArray("scale").get(0));
        Assertions.assertEquals(0.017361589674592462, val.getJSONObject("transform").getJSONArray("scale").get(1));
        Assertions.assertEquals(-180, val.getJSONObject("transform").getJSONArray("translate").get(0));  
        Assertions.assertEquals(-89.99892578124998, val.getJSONObject("transform").getJSONArray("translate").get(1)); 

    }
    
    @Test
    void testFieldParseFormatFromInvalidTopojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_TOPOJSON, null, null, null, null);
        
        // This is an invalid Topojson, it's a Geojson:
        assertThrows(InvalidCastException.class, () -> {
            field.castValue("{ \"type\": \"GeometryCollection\",\n" +
                    "\"geometries\": [\n" +
                    "  { \"type\": \"Point\",\n" +
                    "    \"coordinates\": [100.0, 0.0]\n" +
                    "    },\n" +
                    "  { \"type\": \"LineString\",\n" +
                    "    \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n" +
                    "    }\n" +
                    " ]\n" +
                    "}");
        });
    }

    @Test
    void testCastNumberGroupChar() throws Exception{
        String testValue = "1 564 1020";
        Map<String, Object> options = new HashMap<>();
        options.put("groupChar", " ");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assertions.assertEquals(15641020L, num.intValue());
    }

    @Test
    void testCastNumberDecimalChar() throws Exception{
        String testValue = "1020,123";
        Map<String, Object> options = new HashMap();
        options.put("decimalChar", ",");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assertions.assertEquals(1020.123, num.floatValue(), 0.01);
    }

    @Test
    void testCastNumberNonBare() throws Exception{
        String testValue = "150 EUR";
        Map<String, Object> options = new HashMap();
        options.put("bareNumber", false);

        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);
        Assertions.assertEquals(150, num.intValue());

        testValue = "$125";
        num = field.castValue(testValue, false, options);
        Assertions.assertEquals(125, num.intValue());
    }

    @Test
    void testCastNumberGroupAndDecimalCharAsWellAsNonBare() throws Exception{
        String testValue = "1 564,123 EUR";
        Map<String, Object> options = new HashMap();
        options.put("bareNumber", false);
        options.put("groupChar", " ");
        options.put("decimalChar", ",");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);
        Assertions.assertEquals(1564.123, num.floatValue(), 0.01);

    }


    @Test
    void testFieldParseFormatFromObject() throws Exception{
        ObjectField field = new ObjectField("test");
        JSONObject val = field.castValue("{\"one\": 1, \"two\": 2, \"three\": 3}");
        Assertions.assertEquals(3, val.length()); 
        Assertions.assertEquals(1, val.getInt("one")); 
        Assertions.assertEquals(2, val.getInt("two")); 
        Assertions.assertEquals(3, val.getInt("three")); 
    }
    
    @Test
    void testFieldParseFormatFromArray() throws Exception{
        ArrayField field = new ArrayField("test");
        JSONArray val = field.castValue("[1,2,3,4]");
        
        Assertions.assertEquals(4, val.length()); 
        Assertions.assertEquals(1, val.get(0));
        Assertions.assertEquals(2, val.get(1));
        Assertions.assertEquals(3, val.get(2));
        Assertions.assertEquals(4, val.get(3));
    }
    
    @Test
    void testFieldParseFormatFromDateTime() throws Exception{
        DatetimeField field = new DatetimeField("test");
        DateTime val = field.castValue("2008-08-30T01:45:36.123Z");
        
        Assertions.assertEquals(2008, val.withZone(DateTimeZone.UTC).getYear());
        Assertions.assertEquals(8, val.withZone(DateTimeZone.UTC).getMonthOfYear());
        Assertions.assertEquals(30, val.withZone(DateTimeZone.UTC).getDayOfMonth());
        Assertions.assertEquals(1, val.withZone(DateTimeZone.UTC).getHourOfDay());
        Assertions.assertEquals(45, val.withZone(DateTimeZone.UTC).getMinuteOfHour());
        Assertions.assertEquals("2008-08-30T01:45:36.123Z", val.withZone(DateTimeZone.UTC).toString());
    }
    
    @Test
    void testFieldParseFormatFromDate() throws Exception{
        DateField field = new DateField("test");
        DateTime val = field.castValue("2008-08-30");
        
        Assertions.assertEquals(2008, val.getYear());
        Assertions.assertEquals(8, val.getMonthOfYear());
        Assertions.assertEquals(30, val.getDayOfMonth());
    }
    
    @Test
    void testFieldParseFormatFromTime() throws Exception{
        TimeField field = new TimeField("test");
        DateTime val = field.castValue("14:22:33");
        
        Assertions.assertEquals(14, val.getHourOfDay());
        Assertions.assertEquals(22, val.getMinuteOfHour());
        Assertions.assertEquals(33, val.getSecondOfMinute());
    }
    
    @Test
    void testFieldParseFormatFromYear() throws Exception{
        YearField field = new YearField("test");
        int val = field.castValue("2008");
        Assertions.assertEquals(2008, val);
    }
    
    @Test
    void testFieldParseFormatFromYearmonth() throws Exception{
        YearmonthField field = new YearmonthField("test");
        DateTime val = field.castValue("2008-08");
        
        Assertions.assertEquals(2008, val.getYear());
        Assertions.assertEquals(8, val.getMonthOfYear());
    }
    
    @Test
    void testFieldParseFormatFromNumber() throws Exception{
        IntegerField intField = new IntegerField("intNum");
        NumberField floatField = new NumberField("floatNum");
        
        BigInteger intValPositive1 = intField.castValue("123");
        Assertions.assertEquals(123, intValPositive1.intValue());

        BigInteger intValPositive2 = intField.castValue("+128127");
        Assertions.assertEquals(128127, intValPositive2.intValue());

        BigInteger intValNegative = intField.castValue("-765");
        Assertions.assertEquals(-765, intValNegative.intValue());
             
        Number floatValPositive1 = floatField.castValue("123.9902");
        Assertions.assertEquals(123.9902, floatValPositive1.floatValue(), 0.01);

        Number floatValPositive2 = floatField.castValue("+128127.1929");
        Assertions.assertEquals(128127.1929, floatValPositive2.floatValue(), 0.01);

        Number floatValNegative = floatField.castValue("-765.929");
        Assertions.assertEquals(-765.929, floatValNegative.floatValue(), 0.01);
        
    }
    
    @Test
    void testFieldParseFormatFromBoolean() throws Exception{
        BooleanField field = new BooleanField("test");
        
        Assertions.assertFalse(field.castValue("f"));
        Assertions.assertFalse(field.castValue("F"));
        Assertions.assertFalse(field.castValue("False"));
        Assertions.assertFalse(field.castValue("false"));
        Assertions.assertFalse(field.castValue("FALSE"));
        Assertions.assertFalse(field.castValue("0"));
        Assertions.assertFalse(field.castValue("no"));
        Assertions.assertFalse(field.castValue("NO"));
        Assertions.assertFalse(field.castValue("n"));
        Assertions.assertFalse(field.castValue("N"));

        Assertions.assertTrue(field.castValue("t"));
        Assertions.assertTrue(field.castValue("T"));
        Assertions.assertTrue(field.castValue("True"));
        Assertions.assertTrue(field.castValue("true"));
        Assertions.assertTrue(field.castValue("TRUE"));
        Assertions.assertTrue(field.castValue("1"));
        Assertions.assertTrue(field.castValue("yes"));
        Assertions.assertTrue(field.castValue("YES"));
        Assertions.assertTrue(field.castValue("y"));
        Assertions.assertTrue(field.castValue("Y"));
    }


    */
}
