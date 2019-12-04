package io.frictionlessdata.tableschema.field_tests;

import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import io.frictionlessdata.tableschema.field.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * 
 */
public class FieldCastTest {


    @Test
    public void testFieldCastGeopointDefault() throws Exception{
        GeopointField field = new GeopointField("test", Field.FIELD_FORMAT_DEFAULT, "title", "description", null);
        int[] val = field.castValue("12,21");
        Assertions.assertEquals(12, val[0]);
        Assertions.assertEquals(21, val[1]);
    }
    
    @Test
    public void testFieldCastGeopointArray() throws Exception{
        GeopointField field = new GeopointField("test", Field.FIELD_FORMAT_ARRAY, "title", "description", null);
        int[] val = field.castValue("[45,32]");
        Assertions.assertEquals(45, val[0]);
        Assertions.assertEquals(32, val[1]);   
    }
    
    @Test
    public void testFieldCastGeopointObject() throws Exception{
        GeopointField field = new GeopointField("test", Field.FIELD_FORMAT_OBJECT, null, null, null);
        int[] val = field.castValue("{\"lon\": 67, \"lat\": 19}");
        Assertions.assertEquals(67, val[0]);
        Assertions.assertEquals(19, val[1]);   
    }
    
    @Test
    public void testFieldCastInteger() throws Exception{
        IntegerField field = new IntegerField("test");
        long val = field.castValue("123");
        Assertions.assertEquals(123, val); 
    }
    
    @Test
    public void testFieldCastDuration() throws Exception{
        DurationField field = new DurationField("test");
        Duration val = field.castValue("P2DT3H4M");
        Assertions.assertEquals(183840, val.getSeconds()); 
    }
    
    @Test
    public void testFieldCastValidGeojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_DEFAULT, null, null, null);
        JSONObject val = field.castValue("{\n" +
            "    \"type\": \"Feature\",\n" +
            "    \"properties\": {\n" +
            "        \"name\": \"Coors Field\",\n" +
            "        \"amenity\": \"Baseball Stadium\",\n" +
            "        \"popupContent\": \"This is where the Rockies play!\"\n" +
            "    },\n" +
            "    \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [-104.99404, 39.75621]\n" +
            "    }\n" +
            "}");
        
        Assertions.assertEquals("Feature", val.getString("type"));
        Assertions.assertEquals("Baseball Stadium", val.getJSONObject("properties").getString("amenity"));
        Assertions.assertEquals(-104.99404, val.getJSONObject("geometry").getJSONArray("coordinates").get(0));
        Assertions.assertEquals(39.75621, val.getJSONObject("geometry").getJSONArray("coordinates").get(1));
    }
    
    @Test
    public void testFieldCastInvalidGeojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_DEFAULT, null, null, null);
        assertThrows(InvalidCastException.class, () -> {
            field.castValue("{\n" +
                "    \"type\": \"INVALID_TYPE\",\n" + // The invalidity is here.
                "    \"properties\": {\n" +
                "        \"name\": \"Coors Field\",\n" +
                "        \"amenity\": \"Baseball Stadium\",\n" +
                "        \"popupContent\": \"This is where the Rockies play!\"\n" +
                "    },\n" +
                "    \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [-104.99404, 39.75621]\n" +
                "    }\n" +
                "}");
        });

    }
    
    @Test
    public void testFieldCastValidTopojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_TOPOJSON, null, null, null);

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
        
        /**
        // Another Geosjon to test
        JSONObject val2 = field.castValue("{ \"type\": \"GeometryCollection\",\n" +
            "\"geometries\": [\n" +
            "  { \"type\": \"Point\",\n" +
            "    \"coordinates\": [100.0, 0.0]\n" +
            "    },\n" +
            "  { \"type\": \"LineString\",\n" +
            "    \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n" +
            "    }\n" +
            " ]\n" +
            "}");
        
        Assertions.assertEquals("GeometryCollection", val.getString("type"));
        Assertions.assertEquals("Point", val.getJSONArray("geometries").getJSONObject(0).getString("type"));
        Assertions.assertEquals("LineString", val.getJSONArray("geometries").getJSONObject(1).getString("type"));
        **/
    }
    
    @Test
    public void testFieldCastInvalidTopojson() throws Exception{
        GeojsonField field = new GeojsonField("test", Field.FIELD_FORMAT_TOPOJSON, null, null, null);
        
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
    public void testCastNumberGroupChar() throws Exception{
        String testValue = "1 564 1020";
        Map<String, Object> options = new HashMap<>();
        options.put("groupChar", " ");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assertions.assertEquals(15641020L, num.intValue());
    }

    @Test
    public void testCastNumberDecimalChar() throws Exception{
        String testValue = "1020,123";
        Map<String, Object> options = new HashMap();
        options.put("decimalChar", ",");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assertions.assertEquals(1020.123, num.floatValue(), 0.01);
    }

    @Test
    public void testCastNumberNonBare() throws Exception{
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
    public void testCastNumberGroupAndDecimalCharAsWellAsNonBare() throws Exception{
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
    public void testFieldCastObject() throws Exception{
        ObjectField field = new ObjectField("test");
        JSONObject val = field.castValue("{\"one\": 1, \"two\": 2, \"three\": 3}");
        Assertions.assertEquals(3, val.length()); 
        Assertions.assertEquals(1, val.getInt("one")); 
        Assertions.assertEquals(2, val.getInt("two")); 
        Assertions.assertEquals(3, val.getInt("three")); 
    }
    
    @Test
    public void testFieldCastArray() throws Exception{
        ArrayField field = new ArrayField("test");
        JSONArray val = field.castValue("[1,2,3,4]");
        
        Assertions.assertEquals(4, val.length()); 
        Assertions.assertEquals(1, val.get(0));
        Assertions.assertEquals(2, val.get(1));
        Assertions.assertEquals(3, val.get(2));
        Assertions.assertEquals(4, val.get(3));
    }
    
    @Test
    public void testFieldCastDateTime() throws Exception{
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
    public void testFieldCastDate() throws Exception{
        DateField field = new DateField("test");
        DateTime val = field.castValue("2008-08-30");
        
        Assertions.assertEquals(2008, val.getYear());
        Assertions.assertEquals(8, val.getMonthOfYear());
        Assertions.assertEquals(30, val.getDayOfMonth());
    }
    
    @Test
    public void testFieldCastTime() throws Exception{
        TimeField field = new TimeField("test");
        DateTime val = field.castValue("14:22:33");
        
        Assertions.assertEquals(14, val.getHourOfDay());
        Assertions.assertEquals(22, val.getMinuteOfHour());
        Assertions.assertEquals(33, val.getSecondOfMinute());
    }
    
    @Test
    public void testFieldCastYear() throws Exception{
        YearField field = new YearField("test");
        int val = field.castValue("2008");
        Assertions.assertEquals(2008, val);
    }
    
    @Test
    public void testFieldCastYearmonth() throws Exception{
        YearmonthField field = new YearmonthField("test");
        DateTime val = field.castValue("2008-08");
        
        Assertions.assertEquals(2008, val.getYear());
        Assertions.assertEquals(8, val.getMonthOfYear());
    }
    
    @Test
    public void testFieldCastNumber() throws Exception{
        IntegerField intField = new IntegerField("intNum");
        NumberField floatField = new NumberField("floatNum");
        
        long intValPositive1 = intField.castValue("123");
        Assertions.assertEquals(123, intValPositive1);

        long intValPositive2 = intField.castValue("+128127");
        Assertions.assertEquals(128127, intValPositive2);

        long intValNegative = intField.castValue("-765");
        Assertions.assertEquals(-765, intValNegative);
             
        Number floatValPositive1 = floatField.castValue("123.9902");
        Assertions.assertEquals(123.9902, floatValPositive1.floatValue(), 0.01);

        Number floatValPositive2 = floatField.castValue("+128127.1929");
        Assertions.assertEquals(128127.1929, floatValPositive2.floatValue(), 0.01);

        Number floatValNegative = floatField.castValue("-765.929");
        Assertions.assertEquals(-765.929, floatValNegative.floatValue(), 0.01);
        
    }
    
    @Test
    public void testFieldCastBoolean() throws Exception{
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
    
    @Test
    public void testFieldCastString() throws Exception{
        StringField field = new StringField("test");
        String val = field.castValue("John Doe");
        
        Assertions.assertEquals("John Doe", val);
    }
    
    @Test
    public void testFieldCastAny() throws Exception{   
        //Assertions.fail("Test case not implemented yet.");
    }
}
