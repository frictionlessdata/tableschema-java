package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.time.Duration;
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
    
    @Test
    public void testFieldCastGeopointDefault() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_GEOPOINT, Field.FIELD_FORMAT_DEFAULT, "title", "description");
        int[] val = field.castValue("12,21");
        Assert.assertEquals(12, val[0]);
        Assert.assertEquals(21, val[1]);   
    }
    
    @Test
    public void testFieldCastGeopointArray() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_GEOPOINT, Field.FIELD_FORMAT_ARRAY, "title", "description");
        int[] val = field.castValue("[45,32]");
        Assert.assertEquals(45, val[0]);
        Assert.assertEquals(32, val[1]);   
    }
    
    @Test
    public void testFieldCastGeopointObject() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_GEOPOINT, Field.FIELD_FORMAT_OBJECT);
        int[] val = field.castValue("{\"lon\": 67, \"lat\": 19}");
        Assert.assertEquals(67, val[0]);
        Assert.assertEquals(19, val[1]);   
    }
    
    @Test
    public void testFieldCastInteger() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_INTEGER);
        int val = field.castValue("123");
        Assert.assertEquals(123, val); 
    }
    
    @Test
    public void testFieldCastDuration() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_DURATION);
        Duration val = field.castValue("P2DT3H4M");
        Assert.assertEquals(183840, val.getSeconds()); 
    }
    
    @Test
    public void testFieldCastValidGeojson() throws Exception{
        Field field = new Field("test", Field.FIELD_TYPE_GEOJSON, Field.FIELD_FORMAT_DEFAULT);
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
        Field field = new Field("test", Field.FIELD_TYPE_GEOJSON, Field.FIELD_FORMAT_DEFAULT);
        
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

        Field field = new Field("test", Field.FIELD_TYPE_GEOJSON, Field.FIELD_FORMAT_TOPOJSON);
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
        Field field = new Field("test", Field.FIELD_TYPE_GEOJSON, Field.FIELD_FORMAT_TOPOJSON);
        
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
    public void testFieldCastObject() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_OBJECT);
        JSONObject val = field.castValue("{\"one\": 1, \"two\": 2, \"three\": 3}");
        Assert.assertEquals(3, val.length()); 
        Assert.assertEquals(1, val.getInt("one")); 
        Assert.assertEquals(2, val.getInt("two")); 
        Assert.assertEquals(3, val.getInt("three")); 
    }
    
    @Test
    public void testFieldCastArray() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_ARRAY); 
        JSONArray val = field.castValue("[1,2,3,4]");
        
        Assert.assertEquals(4, val.length()); 
        Assert.assertEquals(1, val.get(0));
        Assert.assertEquals(2, val.get(1));
        Assert.assertEquals(3, val.get(2));
        Assert.assertEquals(4, val.get(3));
    }
    
    @Test
    public void testFieldCastDateTime() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_DATETIME); 
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
        Field field = new Field("test", Field.FIELD_TYPE_DATE); 
        DateTime val = field.castValue("2008-08-30");
        
        Assert.assertEquals(2008, val.getYear());
        Assert.assertEquals(8, val.getMonthOfYear());
        Assert.assertEquals(30, val.getDayOfMonth());
    }
    
    @Test
    public void testFieldCastTime() throws Exception{
        Field field = new Field("test", Field.FIELD_TYPE_TIME); 
        DateTime val = field.castValue("14:22:33");
        
        Assert.assertEquals(14, val.getHourOfDay());
        Assert.assertEquals(22, val.getMinuteOfHour());
        Assert.assertEquals(33, val.getSecondOfMinute());
    }
    
    @Test
    public void testFieldCastYear() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_YEAR); 
        int val = field.castValue("2008");
        Assert.assertEquals(2008, val);
    }
    
    @Test
    public void testFieldCastYearmonth() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_YEARMONTH); 
        DateTime val = field.castValue("2008-08");
        
        Assert.assertEquals(2008, val.getYear());
        Assert.assertEquals(8, val.getMonthOfYear());
    }
    
    @Test
    public void testFieldCastNumber() throws Exception{   
        Field intField = new Field("intNum", Field.FIELD_TYPE_NUMBER);
        Field floatField = new Field("floatNum", Field.FIELD_TYPE_NUMBER);
        
        int intValPositive1 = intField.castValue("123");
        Assert.assertEquals(123, intValPositive1);
        
        int intValPositive2 = intField.castValue("+128127");
        Assert.assertEquals(128127, intValPositive2);
        
        int intValNegative = intField.castValue("-765");
        Assert.assertEquals(-765, intValNegative);
             
        float floatValPositive1 = floatField.castValue("123.9902");
        Assert.assertEquals(123.9902, floatValPositive1, 0.01);
        
        float floatValPositive2 = floatField.castValue("+128127.1929");
        Assert.assertEquals(128127.1929, floatValPositive2, 0.01);
        
        float floatValNegative = floatField.castValue("-765.929");
        Assert.assertEquals(-765.929, floatValNegative, 0.01);
        
    }
    
    @Test
    public void testFieldCastBoolean() throws Exception{  
        Field field = new Field("test", Field.FIELD_TYPE_BOOLEAN);
        
        Assert.assertFalse(field.castValue("f"));
        Assert.assertFalse(field.castValue("F"));
        Assert.assertFalse(field.castValue("False"));
        Assert.assertFalse(field.castValue("FALSE"));
        Assert.assertFalse(field.castValue("0"));
        Assert.assertFalse(field.castValue("no"));
        Assert.assertFalse(field.castValue("NO"));
        Assert.assertFalse(field.castValue("n"));
        Assert.assertFalse(field.castValue("N"));

        Assert.assertTrue(field.castValue("t"));
        Assert.assertTrue(field.castValue("T"));
        Assert.assertTrue(field.castValue("True"));
        Assert.assertTrue(field.castValue("TRUE"));
        Assert.assertTrue(field.castValue("1"));
        Assert.assertTrue(field.castValue("yes"));
        Assert.assertTrue(field.castValue("YES"));
        Assert.assertTrue(field.castValue("y"));
        Assert.assertTrue(field.castValue("Y"));
    }
    
    @Test
    public void testFieldCastString() throws Exception{   
        Field field = new Field("test", Field.FIELD_TYPE_STRING); 
        String val = field.castValue("John Doe");
        
        Assert.assertEquals("John Doe", val);
    }
    
    @Test
    public void testFieldCastAny() throws Exception{   
        //Assert.fail("Test case not implemented yet.");
    }
}
