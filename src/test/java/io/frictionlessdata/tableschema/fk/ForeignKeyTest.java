package io.frictionlessdata.tableschema.fk;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import io.frictionlessdata.tableschema.exceptions.ForeignKeyException;
import org.junit.Assert;
import org.json.JSONArray;

/**
 *
 */
public class ForeignKeyTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void validStringFieldsTest() throws ForeignKeyException{
        Reference ref = new Reference("aResource", "refField", true);
        ForeignKey fk = new ForeignKey("fkField", ref, true);
        
        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assert.assertNotNull(fk);
    }
    
    @Test
    public void validArrayFieldsTest() throws ForeignKeyException{
        JSONArray refFields = new JSONArray();
        refFields.put("refField1");
        refFields.put("refField2");
        
        Reference ref = new Reference("aResource", refFields, true);
        
        JSONArray fkFields = new JSONArray();
        fkFields.put("fkField1");
        fkFields.put("fkField2");
        
        ForeignKey fk = new ForeignKey(fkFields, ref, true);
        
        // Validation set to strict=true and no exception has been thrown.
        // Test passes.
        Assert.assertNotNull(fk);
    }
    
    @Test
    public void nullFieldsTest() throws ForeignKeyException{
        Reference ref = new Reference("aResource", "aField", true);
        
        exception.expectMessage("A foreign key must have the fields and reference properties.");
        ForeignKey fk = new ForeignKey(null, ref, true);
    }
    
    @Test
    public void nullReferenceTest() throws ForeignKeyException{
        ForeignKey fk = new ForeignKey();
        fk.setFields("aField");
        
        exception.expectMessage("A foreign key must have the fields and reference properties.");
        fk.validate();
    }
    
    @Test
    public void nullFieldsAndReferenceTest() throws ForeignKeyException{
        ForeignKey fk = new ForeignKey();
        exception.expectMessage("A foreign key must have the fields and reference properties.");
        fk.validate();
    }
    
    @Test
    public void fieldsNotStringOrArrayTest() throws ForeignKeyException{
        Reference ref = new Reference("aResource", "aField", true);
        
        exception.expectMessage("The foreign key's fields property must be a string or an array.");
        ForeignKey fk = new ForeignKey(25, ref, true);
    }
    
    @Test
    public void fkFieldsIsStringAndRefFieldsIsArrayTest() throws ForeignKeyException{
        JSONArray refFields = new JSONArray();
        refFields.put("field1");
        refFields.put("field2");
        refFields.put("field3");
        
        Reference ref = new Reference("aResource", refFields, true);
        
        exception.expectMessage("The reference's fields property must be a string if the outer fields is a string.");
        ForeignKey fk = new ForeignKey("aStringField", ref, true);
    }
    
    @Test
    public void fkFieldsIsArrayAndRefFieldsIsStringTest() throws ForeignKeyException{
        Reference ref = new Reference("aResource", "aStringField", true);
        
        JSONArray fkFields = new JSONArray();
        fkFields.put("field1");
        fkFields.put("field2");
        fkFields.put("field3");
        
        exception.expectMessage("The reference's fields property must be an array if the outer fields is an array.");
        ForeignKey fk = new ForeignKey(fkFields, ref, true);
    }
    
    @Test
    public void fkAndRefFieldsDifferentSizeArrayTest() throws ForeignKeyException{
        JSONArray refFields = new JSONArray();
        refFields.put("refField1");
        refFields.put("refField2");
        refFields.put("refField3");
        
        Reference ref = new Reference("aResource", refFields, true);
        
        JSONArray fkFields = new JSONArray();
        fkFields.put("field1");
        fkFields.put("field2");
        
        exception.expectMessage("The reference's fields property must an array of the same length as the outer.");
        ForeignKey fk = new ForeignKey(fkFields, ref, true);
    }
}
