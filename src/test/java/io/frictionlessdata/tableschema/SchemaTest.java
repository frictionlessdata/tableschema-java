package io.frictionlessdata.tableschema;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * 
 */
public class SchemaTest {
    
    @Test
    public void testCreateSchemaFromValidSchemaJson(){ 
        JSONObject schemaJsonObj = new JSONObject();
       
        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new Field("id", "integer");
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        
        Schema validSchema = new Schema(schemaJsonObj);
        Assert.assertTrue(validSchema.validate());
    }
    
    @Test
    public void testCreateSchemaFromInvalidSchemaJson(){  
        JSONObject schemaJsonObj = new JSONObject();
       
        schemaJsonObj.put("fields", new JSONArray());
        Field nameField = new Field("id", "integer");
        Field invalidField = new Field("coordinates", "invalid");
        schemaJsonObj.getJSONArray("fields").put(nameField.getJson());
        schemaJsonObj.getJSONArray("fields").put(invalidField.getJson());
        
        Schema invalidSchema = new Schema(schemaJsonObj);
        Assert.assertFalse(invalidSchema.validate());
    }
     
    @Test
    public void testAddValidField(){
        Field nameField = new Field("id", "integer");
        Schema validSchema = new Schema();
        validSchema.addField(nameField);
        
        Assert.assertEquals(1, validSchema.getFields().size()); 
    }
    
    @Test
    public void testAddInvalidField(){
        Field nameField = new Field("id", "integer");
        Field invalidField = new Field("title", "invalid");
        Field geopointField = new Field("coordinates", "geopoint"); 
        
        Schema validSchema = new Schema();
        validSchema.addField(nameField);
        validSchema.addField(invalidField); // will be ignored
        validSchema.addField(geopointField);
        
        Assert.assertEquals(2, validSchema.getFields().size());
        Assert.assertNull(validSchema.getField("title"));
        Assert.assertNotNull(validSchema.getField("id"));
        Assert.assertNotNull(validSchema.getField("coordinates"));
    }

}
