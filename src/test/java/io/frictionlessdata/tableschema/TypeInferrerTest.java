package io.frictionlessdata.tableschema;

import java.util.HashMap;
import java.util.Map;

import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.field.IntegerField;
import io.frictionlessdata.tableschema.field.NumberField;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * 
 */
public class TypeInferrerTest {

    @Test
    public void testInferNumberGroupChar() throws Exception{
        Map<String, Object> options = new HashMap<>();
        options.put("groupChar", " ");
        NumberField field = new NumberField("int field");
        Number num = field.castValue("1 564 1020", false, options);
        
        Assert.assertEquals(15641020L, num.intValue());
    }
    /*
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
    }*/
}
