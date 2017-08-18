package io.frictionlessdata.tableschema;


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * 
 */
public class TypeInfererTest {
    
    TypeInferer typeInferer = new TypeInferer();
    
    @Test
    public void testCastDatetime(){
        
        // Test with valid dates
        String validDatetimeString = "2008-08-30T01:45:36.123Z";
        
        try{
            DateTime dt = this.typeInferer.castDatetime("default", validDatetimeString).withZone(DateTimeZone.UTC);
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
            DateTime dt = this.typeInferer.castDate("default", validDateString);
            Assert.assertEquals(2008, dt.getYear());
            Assert.assertEquals(8, dt.getMonthOfYear());
            Assert.assertEquals(30, dt.getDayOfMonth());

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Date string into DateTime object.");
        }  
    }
    
    @Test
    public void testCastYear(){
        
        // Test with valid dates
        String validYearString = "2008";
        
        try{
            int year = this.typeInferer.castYear("default", validYearString);
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
            DateTime dt = this.typeInferer.castYearmonth("default", validYearmonthString);
            Assert.assertEquals(2008, dt.getYear());
            Assert.assertEquals(8, dt.getMonthOfYear());

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Yearmonth string into DateTime object.");
        }  
    }
    
    @Test
    public void testCastGeopoint(){
        try{
            int[] geopointDefault = this.typeInferer.castGeopoint("default", "34,23");
            Assert.assertEquals(34, geopointDefault[0]);
            Assert.assertEquals(23, geopointDefault[1]);
            
            int[] geopointArray = this.typeInferer.castGeopoint("array", "[10,67]");
            Assert.assertEquals(10, geopointArray[0]);
            Assert.assertEquals(67, geopointArray[1]);
            
            int[] geopointObject = this.typeInferer.castGeopoint("object", "{\"lon\": 12, \"lat\": 44}");
            Assert.assertEquals(12, geopointObject[0]);
            Assert.assertEquals(44, geopointObject[1]);
            
        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Geopoint String into Integer[] object.");
        }
    }
    
    @Test
    public void testCastInteger(){
        try{
            Assert.assertEquals(2, this.typeInferer.castInteger("default", "2"));
            Assert.assertEquals(10, this.typeInferer.castInteger("default", "10"));
            Assert.assertEquals(123123, this.typeInferer.castInteger("default", "123123"));
            
        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid Integer String into primitive int type.");
        }
    }
    
    @Test
    public void testCastBoolean(){
        try{
            Assert.assertFalse(this.typeInferer.castBoolean("default", "f"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "F"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "False"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "FALSE"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "0"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "no"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "NO"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "n"));
            Assert.assertFalse(this.typeInferer.castBoolean("default", "N"));
            
            Assert.assertTrue(this.typeInferer.castBoolean("default", "t"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "T"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "True"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "TRUE"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "1"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "yes"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "YES"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "y"));
            Assert.assertTrue(this.typeInferer.castBoolean("default", "Y"));

        }catch(TypeInferringException tie){
            Assert.fail("Failed to cast valid boolean String into boolean object.");
        } 
    }
    
    @Test
    public void testCastArray(){
        
    }
    
    @Test
    public void testCastObject(){
        
    }

}
