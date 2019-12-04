package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.frictionlessdata.tableschema.field.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * 
 */
public class FieldTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static String testJson = "{\"name\":\"city\",\"format\":\"\",\"description\":\"\",\"title\":\"\",\"type\":\"string\",\"constraints\":{}}";


    @Test
    public void testFieldCreationFromString() throws Exception{
        Field testField = new ObjectField(new JSONObject(testJson));
        Assert.assertEquals(testField.getName(), "city");
    }

        @Test
    public void testFieldCastGeopointDefault() throws Exception{
        GeoPointField field = new GeoPointField("test", Field.FIELD_FORMAT_DEFAULT, "title", "description", null);
        int[] val = field.castValue("12,21");
        Assert.assertEquals(12, val[0]);
        Assert.assertEquals(21, val[1]);   
    }
    
    @Test
    public void testFieldCastGeopointArray() throws Exception{
        GeoPointField field = new GeoPointField("test", Field.FIELD_FORMAT_ARRAY, "title", "description", null);
        int[] val = field.castValue("[45,32]");
        Assert.assertEquals(45, val[0]);
        Assert.assertEquals(32, val[1]);   
    }
    
    @Test
    public void testFieldCastGeopointObject() throws Exception{
        GeoPointField field = new GeoPointField("test", Field.FIELD_FORMAT_OBJECT, null, null, null);
        int[] val = field.castValue("{\"lon\": 67, \"lat\": 19}");
        Assert.assertEquals(67, val[0]);
        Assert.assertEquals(19, val[1]);   
    }
    
    @Test
    public void testFieldCastInteger() throws Exception{
        IntegerField field = new IntegerField("test");
        long val = field.castValue("123");
        Assert.assertEquals(123, val); 
    }
    
    @Test
    public void testFieldCastDuration() throws Exception{
        DurationField field = new DurationField("test");
        Duration val = field.castValue("P2DT3H4M");
        Assert.assertEquals(183840, val.getSeconds()); 
    }
    
    @Test
    public void testFieldCastValidGeojson() throws Exception{
        GeoJsonField field = new GeoJsonField("test", Field.FIELD_FORMAT_DEFAULT, null, null, null);
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
        
        Assert.assertEquals("Feature", val.getString("type"));
        Assert.assertEquals("Baseball Stadium", val.getJSONObject("properties").getString("amenity"));
        Assert.assertEquals(-104.99404, val.getJSONObject("geometry").getJSONArray("coordinates").get(0));
        Assert.assertEquals(39.75621, val.getJSONObject("geometry").getJSONArray("coordinates").get(1));
    }
    
    @Test
    public void testFieldCastInvalidGeojson() throws Exception{
        GeoJsonField field = new GeoJsonField("test", Field.FIELD_FORMAT_DEFAULT, null, null, null);
        
        exception.expect(InvalidCastException.class);
        JSONObject val = field.castValue("{\n" +
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
    }
    
    @Test
    public void testFieldCastValidTopojson() throws Exception{
        GeoJsonField field = new GeoJsonField("test", Field.FIELD_FORMAT_TOPOJSON, null, null, null);

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
        
        Assert.assertEquals("Topology", val.getString("type"));
        Assert.assertEquals(0.036003600360036005, val.getJSONObject("transform").getJSONArray("scale").get(0));
        Assert.assertEquals(0.017361589674592462, val.getJSONObject("transform").getJSONArray("scale").get(1));
        Assert.assertEquals(-180, val.getJSONObject("transform").getJSONArray("translate").get(0));  
        Assert.assertEquals(-89.99892578124998, val.getJSONObject("transform").getJSONArray("translate").get(1)); 
        
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
        
        Assert.assertEquals("GeometryCollection", val.getString("type"));
        Assert.assertEquals("Point", val.getJSONArray("geometries").getJSONObject(0).getString("type"));
        Assert.assertEquals("LineString", val.getJSONArray("geometries").getJSONObject(1).getString("type"));
        **/
    }
    
    @Test
    public void testFieldCastInvalidTopojson() throws Exception{
        GeoJsonField field = new GeoJsonField("test", Field.FIELD_FORMAT_TOPOJSON, null, null, null);
        
        // This is an invalid Topojson, it's a Geojson:
        exception.expect(InvalidCastException.class);
        JSONObject val = field.castValue("{ \"type\": \"GeometryCollection\",\n" +
            "\"geometries\": [\n" +
            "  { \"type\": \"Point\",\n" +
            "    \"coordinates\": [100.0, 0.0]\n" +
            "    },\n" +
            "  { \"type\": \"LineString\",\n" +
            "    \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n" +
            "    }\n" +
            " ]\n" +
            "}");
        
    }

    @Test
    public void testCastNumberGroupChar() throws Exception{
        String testValue = "1 564 1020";
        Map<String, Object> options = new HashMap<>();
        options.put("groupChar", " ");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assert.assertEquals(15641020L, num.intValue());
    }

    @Test
    public void testCastNumberDecimalChar() throws Exception{
        String testValue = "1020,123";
        Map<String, Object> options = new HashMap();
        options.put("decimalChar", ",");
        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);

        Assert.assertEquals(1020.123, num.floatValue(), 0.01);
    }

    @Test
    public void testCastNumberNonBare() throws Exception{
        String testValue = "150 EUR";
        Map<String, Object> options = new HashMap();
        options.put("bareNumber", false);

        NumberField field = new NumberField("int field");
        Number num = field.castValue(testValue, false, options);
        Assert.assertEquals(150, num.intValue());

        testValue = "$125";
        num = field.castValue(testValue, false, options);
        Assert.assertEquals(125, num.intValue());
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
        Assert.assertEquals(1564.123, num.floatValue(), 0.01);

    }


    @Test
    public void testFieldCastObject() throws Exception{
        ObjectField field = new ObjectField("test");
        JSONObject val = field.castValue("{\"one\": 1, \"two\": 2, \"three\": 3}");
        Assert.assertEquals(3, val.length()); 
        Assert.assertEquals(1, val.getInt("one")); 
        Assert.assertEquals(2, val.getInt("two")); 
        Assert.assertEquals(3, val.getInt("three")); 
    }
    
    @Test
    public void testFieldCastArray() throws Exception{
        ArrayField field = new ArrayField("test");
        JSONArray val = field.castValue("[1,2,3,4]");
        
        Assert.assertEquals(4, val.length()); 
        Assert.assertEquals(1, val.get(0));
        Assert.assertEquals(2, val.get(1));
        Assert.assertEquals(3, val.get(2));
        Assert.assertEquals(4, val.get(3));
    }
    
    @Test
    public void testFieldCastDateTime() throws Exception{
        DateTimeField field = new DateTimeField("test");
        DateTime val = field.castValue("2008-08-30T01:45:36.123Z");
        
        Assert.assertEquals(2008, val.withZone(DateTimeZone.UTC).getYear());
        Assert.assertEquals(8, val.withZone(DateTimeZone.UTC).getMonthOfYear());
        Assert.assertEquals(30, val.withZone(DateTimeZone.UTC).getDayOfMonth());
        Assert.assertEquals(1, val.withZone(DateTimeZone.UTC).getHourOfDay());
        Assert.assertEquals(45, val.withZone(DateTimeZone.UTC).getMinuteOfHour());
        Assert.assertEquals("2008-08-30T01:45:36.123Z", val.withZone(DateTimeZone.UTC).toString());
    }
    
    @Test
    public void testFieldCastDate() throws Exception{
        DateField field = new DateField("test");
        DateTime val = field.castValue("2008-08-30");
        
        Assert.assertEquals(2008, val.getYear());
        Assert.assertEquals(8, val.getMonthOfYear());
        Assert.assertEquals(30, val.getDayOfMonth());
    }
    
    @Test
    public void testFieldCastTime() throws Exception{
        TimeField field = new TimeField("test");
        DateTime val = field.castValue("14:22:33");
        
        Assert.assertEquals(14, val.getHourOfDay());
        Assert.assertEquals(22, val.getMinuteOfHour());
        Assert.assertEquals(33, val.getSecondOfMinute());
    }
    
    @Test
    public void testFieldCastYear() throws Exception{
        YearField field = new YearField("test");
        int val = field.castValue("2008");
        Assert.assertEquals(2008, val);
    }
    
    @Test
    public void testFieldCastYearmonth() throws Exception{
        YearMonthField field = new YearMonthField("test");
        DateTime val = field.castValue("2008-08");
        
        Assert.assertEquals(2008, val.getYear());
        Assert.assertEquals(8, val.getMonthOfYear());
    }
    
    @Test
    public void testFieldCastNumber() throws Exception{
        IntegerField intField = new IntegerField("intNum");
        NumberField floatField = new NumberField("floatNum");
        
        long intValPositive1 = intField.castValue("123");
        Assert.assertEquals(123, intValPositive1);

        long intValPositive2 = intField.castValue("+128127");
        Assert.assertEquals(128127, intValPositive2);

        long intValNegative = intField.castValue("-765");
        Assert.assertEquals(-765, intValNegative);
             
        Number floatValPositive1 = floatField.castValue("123.9902");
        Assert.assertEquals(123.9902, floatValPositive1.floatValue(), 0.01);

        Number floatValPositive2 = floatField.castValue("+128127.1929");
        Assert.assertEquals(128127.1929, floatValPositive2.floatValue(), 0.01);

        Number floatValNegative = floatField.castValue("-765.929");
        Assert.assertEquals(-765.929, floatValNegative.floatValue(), 0.01);
        
    }
    
    @Test
    public void testFieldCastBoolean() throws Exception{
        BooleanField field = new BooleanField("test");
        
        Assert.assertFalse(field.castValue("f"));
        Assert.assertFalse(field.castValue("F"));
        Assert.assertFalse(field.castValue("False"));
        Assert.assertFalse(field.castValue("false"));
        Assert.assertFalse(field.castValue("FALSE"));
        Assert.assertFalse(field.castValue("0"));
        Assert.assertFalse(field.castValue("no"));
        Assert.assertFalse(field.castValue("NO"));
        Assert.assertFalse(field.castValue("n"));
        Assert.assertFalse(field.castValue("N"));

        Assert.assertTrue(field.castValue("t"));
        Assert.assertTrue(field.castValue("T"));
        Assert.assertTrue(field.castValue("True"));
        Assert.assertTrue(field.castValue("true"));
        Assert.assertTrue(field.castValue("TRUE"));
        Assert.assertTrue(field.castValue("1"));
        Assert.assertTrue(field.castValue("yes"));
        Assert.assertTrue(field.castValue("YES"));
        Assert.assertTrue(field.castValue("y"));
        Assert.assertTrue(field.castValue("Y"));
    }
    
    @Test
    public void testFieldCastString() throws Exception{
        StringField field = new StringField("test");
        String val = field.castValue("John Doe");
        
        Assert.assertEquals("John Doe", val);
    }
    
    @Test
    public void testFieldCastAny() throws Exception{   
        //Assert.fail("Test case not implemented yet.");
    }
}
