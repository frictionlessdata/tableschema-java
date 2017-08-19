package io.frictionlessdata.tableschema;

import java.io.InputStream;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * 
 */
public class Schema {
    
    private JSONObject schema = null;
    private org.everit.json.schema.Schema tableJsonSchema = null;
    
    public Schema(){
        this.schema = new JSONObject();
        schema.put("fields", new JSONArray());
        
        // Init for validation
        InputStream tableSchemaInputStream = TypeInferer.class.getResourceAsStream("/schemas/table-schema.json");
        JSONObject rawTableJsonSchema = new JSONObject(new JSONTokener(tableSchemaInputStream));
        this.tableJsonSchema = SchemaLoader.load(rawTableJsonSchema);
        
    }
    
    public void addField(Field fieldObj){
        this.addField(fieldObj.getJson());
    }
    
    
    public void addField(JSONObject fieldJson){
        this.schema.getJSONArray("fields").put(fieldJson);
        
        try{
            this.tableJsonSchema.validate(this.schema);         
            // No exception thrown? This means that the schema is valid.
        }catch(ValidationException ve){
            // If an Exception is thrown it means that the field that was justed added invalidates the schema.
            // We want to ignore this update on the scheme because now the updated version of the schema fails validation.
            // Simply remove last item that was added
            int position = this.schema.getJSONArray("fields").length();
            this.schema.getJSONArray("fields").remove(position-1);
            ve.getViolatedSchema();
            ve.getErrorMessage();
            String blah = "as;";
        }
    }
    
    public void validate() throws ValidationException{
        try{
            this.tableJsonSchema.validate(this.schema);
        }catch(ValidationException ve){
            throw ve;
        }
    }
    
    public JSONObject getJson(){
        return this.schema;
    }
}
