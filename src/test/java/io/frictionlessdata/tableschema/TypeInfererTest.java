package io.frictionlessdata.tableschema;


import io.frictionlessdata.tableschema.TypeInferer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * 
 */
public class TypeInfererTest {
    
    //TypeInferer typeInferer = new TypeInferer();
    
    @Test
    public void testDurationInferring(){
        
        // Test with valid dates
        String[] validISO8601Dates = new String[]{
            "2008-08-30T01:45:36.123Z",
            "2008-08-30T01:45:36",
            "2008-08-30"};
        /**
        for (String date: validISO8601Dates) {           
            Assert.assertTrue(typeInferer.castDuration(date));
        }**/
        
        // Test with invalid dates
        String[] invalidISO8601Dates = new String[]{
            "2008-08-30T01:45:36.",
            "2008-08-30T"};
        
        /**
        for (String date: invalidISO8601Dates) {           
            Assert.assertFalse(typeInferer.castDuration(date));
        }**/
        
    }

}
