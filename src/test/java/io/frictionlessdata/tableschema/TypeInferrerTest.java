package io.frictionlessdata.tableschema;


import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * The usefulness of these test cases is dubious since pretty much
 * the same code blocks are also tested in FieldTest.
 */
public class TypeInferrerTest {
    
    TypeInferrer typeInferrer = new TypeInferrer();
    
    @Test
    public void testCastDatetime(){
        
        // Test with valid dates
        String validDatetimeString = "2008-08-30T01:45:36.123Z";
        
        try{
            DateTime dt = this.typeInferrer.castDatetime("default", validDatetimeString).withZone(DateTimeZone.UTC);
            Assert.assertEquals(2008, dt.getYear());
            Assert.assertEquals(8, dt.getMonthOfYear());
            Assert.assertEquals(30, dt.getDayOfMonth());
            Assert.assertEquals(1, dt.getHourOfDay());
            Assert.assertEquals(45, dt.getMinuteOfHour());
            Assert.assertEquals(validDatetimeString, dt.toString());

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid DateTime string into DateTime object.");
        }  
    }
    
    @Test
    public void testCastDate(){
        
        // Test with valid dates
        String validDateString = "2008-08-30";
        
        try{
            DateTime dt = this.typeInferrer.castDate("default", validDateString);
            Assert.assertEquals(2008, dt.getYear());
            Assert.assertEquals(8, dt.getMonthOfYear());
            Assert.assertEquals(30, dt.getDayOfMonth());

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Date string into DateTime object.");
        }  
    }
    
    @Test
    public void testCastTime(){
        // Test with valid time
        String validTimeString = "14:22:33";
        
        try{
            DateTime time = this.typeInferrer.castTime("default", validTimeString);
            Assert.assertEquals(14, time.getHourOfDay());
            Assert.assertEquals(22, time.getMinuteOfHour());
            Assert.assertEquals(33, time.getSecondOfMinute());
        
        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Time string into DateTime.");
        } 
    }
    
    @Test
    public void testCastYear(){
        
        // Test with valid dates
        String validYearString = "2008";
        
        try{
            int year = this.typeInferrer.castYear("default", validYearString);
            Assert.assertEquals(2008, year);

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Year string into int.");
        }  
    }
    
    @Test
    public void testCastYearmonth(){
        
        // Test with valid dates
        String validYearmonthString = "2008-08";
        
        try{
            DateTime dt = this.typeInferrer.castYearmonth("default", validYearmonthString);
            Assert.assertEquals(2008, dt.getYear());
            Assert.assertEquals(8, dt.getMonthOfYear());

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Yearmonth string into DateTime object.");
        }  
    }
    
    @Test
    public void testCastGeopoint(){
        try{
            int[] geopointDefault = this.typeInferrer.castGeopoint("default", "34,23");
            Assert.assertEquals(34, geopointDefault[0]);
            Assert.assertEquals(23, geopointDefault[1]);
            
            int[] geopointArray = this.typeInferrer.castGeopoint("array", "[10,67]");
            Assert.assertEquals(10, geopointArray[0]);
            Assert.assertEquals(67, geopointArray[1]);
            
            int[] geopointObject = this.typeInferrer.castGeopoint("object", "{\"lon\": 12, \"lat\": 44}");
            Assert.assertEquals(12, geopointObject[0]);
            Assert.assertEquals(44, geopointObject[1]);
            
        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Geopoint String into Integer[] object.");
        }
    }
    
    @Test
    public void testCastInteger(){
        try{
            Assert.assertEquals(2, this.typeInferrer.castInteger("default", "2"));
            Assert.assertEquals(10, this.typeInferrer.castInteger("default", "10"));
            Assert.assertEquals(123123, this.typeInferrer.castInteger("default", "123123"));
            
        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Integer String into primitive int type.");
        }
    }
    
    @Test
    public void testCastBoolean(){
        try{
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "f"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "F"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "False"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "FALSE"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "0"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "no"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "NO"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "n"));
            Assert.assertFalse(this.typeInferrer.castBoolean("default", "N"));
            
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "t"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "T"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "True"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "TRUE"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "1"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "yes"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "YES"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "y"));
            Assert.assertTrue(this.typeInferrer.castBoolean("default", "Y"));

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid boolean String into boolean object.");
        } 
    }
    
    @Test
    public void testDuration(){
        try{
            Assert.assertEquals(183840, this.typeInferrer.castDuration("default", "P2DT3H4M").getSeconds());
            Assert.assertEquals(900, this.typeInferrer.castDuration("default", "PT15M").getSeconds());
            Assert.assertEquals(20, this.typeInferrer.castDuration("default", "PT20.345S").getSeconds());  
            
        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Duration String into Duration object.");
        }
    }
    
    @Test
    public void testCastArray() throws Exception{
        String arrStr = "[1,2,3,4]";
        JSONArray arr = this.typeInferrer.castArray("default", arrStr);
        
        Assert.assertEquals(1, arr.get(0));
        Assert.assertEquals(2, arr.get(1));
        Assert.assertEquals(3, arr.get(2));
        Assert.assertEquals(4, arr.get(3));
    }
    
    @Test
    public void testCastObject() throws Exception{
        String objStr = "{\"one\": 1, \"two\": 2, \"three\": 3}";
        JSONObject obj = this.typeInferrer.castObject("default", objStr);
        
        Assert.assertEquals(1, obj.get("one"));
        Assert.assertEquals(2, obj.get("two"));
        Assert.assertEquals(3, obj.get("three"));
    }

}
