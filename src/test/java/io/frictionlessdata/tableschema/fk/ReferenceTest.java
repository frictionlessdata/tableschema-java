package io.frictionlessdata.tableschema.fk;

import io.frictionlessdata.tableschema.exceptions.ForeignKeyException;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 */
public class ReferenceTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void validStringFieldsReferenceTest() throws ForeignKeyException{
        Reference ref = new Reference("resource", "field");
        
        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assert.assertNotNull(ref);
    }
    
    @Test
    public void validArrayFieldsReferenceTest() throws ForeignKeyException{
        JSONArray fields = new JSONArray();
        fields.put("field1");
        fields.put("field2");
        
        Reference ref = new Reference("resource", fields);
        
        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assert.assertNotNull(ref);
    }

    @Test
    public void nullFieldsTest() throws ForeignKeyException{
        exception.expectMessage("A foreign key's reference must have the fields and resource properties.");
        Reference ref = new Reference(null, "resource", true);
    }
    
    @Test
    public void nullResourceTest() throws ForeignKeyException{
        Reference ref = new Reference();
        ref.setFields("aField");
        
        exception.expectMessage("A foreign key's reference must have the fields and resource properties.");
        ref.validate();
    }
    
    @Test
    public void nullFieldsAndResourceTest() throws ForeignKeyException{
        Reference ref = new Reference();
        exception.expectMessage("A foreign key's reference must have the fields and resource properties.");
        ref.validate();
    }
    
    @Test
    public void invalidFieldsTypeTest() throws ForeignKeyException{
        exception.expectMessage("The foreign key's reference fields property must be a string or an array.");
        Reference ref = new Reference("resource", 123, true);
    }
}
