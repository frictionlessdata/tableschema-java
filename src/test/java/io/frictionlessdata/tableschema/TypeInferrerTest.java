package io.frictionlessdata.tableschema;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * 
 */
public class TypeInferrerTest {

    @Test
    public void testInferNumbeGroupChar() throws Exception{
        Map<String, Object> options = new HashMap();
        options.put("groupChar", " ");
        int num = (int)TypeInferrer.getInstance().castNumber(Field.FIELD_FORMAT_DEFAULT, "1 564 1020", options);
        
        Assert.assertEquals(15641020, num);
    }
    
    @Test
    public void testInferNumberDecimalChar() throws Exception{  
        Map<String, Object> options = new HashMap();
        options.put("decimalChar", ",");
        float num = (float)TypeInferrer.getInstance().castNumber(Field.FIELD_FORMAT_DEFAULT, "1020,123", options);
        
        Assert.assertEquals(1020.123, num, 0.01);
    }
    
    @Test
    public void testInferNumberNonBare() throws Exception{  
        Map<String, Object> options = new HashMap();
        options.put("bareNumber", false);
        
        int numEUR = (int)TypeInferrer.getInstance().castNumber(Field.FIELD_FORMAT_DEFAULT, "150 EUR", options);
        Assert.assertEquals(150, numEUR);
        
        int numDollarSign = (int)TypeInferrer.getInstance().castNumber(Field.FIELD_FORMAT_DEFAULT, "$125", options);
        Assert.assertEquals(125, numDollarSign);
    }
    
    @Test
    public void testInferNumberGroupAndDecimalCharAsWellAsNonBare() throws Exception{  
        Map<String, Object> options = new HashMap();
        options.put("bareNumber", false);
        options.put("groupChar", " ");
        options.put("decimalChar", ",");
        float num = (float)TypeInferrer.getInstance().castNumber(Field.FIELD_FORMAT_DEFAULT, "1 564,123 EUR", options);
        
        Assert.assertEquals(1564.123, num, 0.01);
    }
}
