package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
   
    private org.everit.json.schema.Schema tableJsonSchema = null;
    private List<Field> fields = new ArrayList();
    
    public Schema(){
        initValidator();
    }
    
    /**
     * Read and validate a table schema  with JSON Object descriptor.
     * @param schema 
     */
    public Schema(JSONObject schema){
        initValidator(); 
        setFieldsFromSchemaJson(schema);
    }
    
    /**
     * Read and validate a table schema with remote descriptor.
     * @param schemaUrl
     * @throws Exception 
     */
    public Schema(URL schemaUrl) throws Exception{
        initValidator();
        InputStreamReader inputStreamReader = new InputStreamReader(schemaUrl.openStream(), "UTF-8");
        initSchemaFromStream(inputStreamReader);
    }
    
    /**
     * Read and validate a table schema with local descriptor.
     * @param schemaFilePath
     * @throws Exception 
     */
    public Schema(String schemaFilePath) throws Exception{
        initValidator(); 
        InputStream is = new FileInputStream(schemaFilePath);
        InputStreamReader inputStreamReader = new InputStreamReader(is);
        initSchemaFromStream(inputStreamReader);
    }
    
    /**
     * Create schema use list of fields. 
     * @param fields 
     */
    public Schema(List<Field> fields){
        initValidator(); 
        this.fields = fields;
    }
    
    /**
     * Initializes the schema from given stream.
     * Used for Schema class instanciation with remote or local schema file.
     * @param schemaStreamReader
     * @throws Exception 
     */
    private void initSchemaFromStream(InputStreamReader schemaStreamReader) throws Exception{
        BufferedReader br = new BufferedReader(schemaStreamReader);
        String line = br.readLine();

        StringBuilder sb = new StringBuilder();
        while(line != null){
            sb.append(line);
            line = br.readLine();
        }

        String schemaString = sb.toString();
        JSONObject schemaJson = new JSONObject(schemaString);
        
        setFieldsFromSchemaJson(schemaJson);
    }
    
    private void setFieldsFromSchemaJson(JSONObject schema){
        if(schema.has("fields")){
            Iterator iter = schema.getJSONArray("fields").iterator();
            while(iter.hasNext()){
                JSONObject fieldJsonObj = (JSONObject)iter.next();
                Field field = new Field(fieldJsonObj);
                this.fields.add(field);
            }  
        }
    }
    
    private void initValidator(){
        // Init for validation
        InputStream tableSchemaInputStream = TypeInferrer.class.getResourceAsStream("/schemas/table-schema.json");
        JSONObject rawTableJsonSchema = new JSONObject(new JSONTokener(tableSchemaInputStream));
        this.tableJsonSchema = SchemaLoader.load(rawTableJsonSchema);
    }
           
    
    public void addField(Field field){
        this.fields.add(field);
        
        try{
            this.tableJsonSchema.validate(this.getJson());         
            // No exception thrown? This means that the schema is valid.
        }catch(ValidationException ve){
            // If an Exception is thrown it means that the field that was justed added invalidates the schema.
            // We want to ignore this update on the scheme because now the updated version of the schema fails validation.
            // Simply remove last item that was added
            this.fields.remove(this.fields.size()-1);
        }
    }
    
    
    public void addField(JSONObject fieldJson){
        Field field = new Field(fieldJson);
        this.addField(field);
    }
    
    public List<Field> getFields(){
        return this.fields;
    }
    
    public Field getField(String name){
        Iterator<Field> iter = this.fields.iterator();
        while(iter.hasNext()){
            Field field = iter.next();
            if(field.getName().equalsIgnoreCase(name)){
                return field;
            }
        }
        return null;
    }
    
    public boolean hasField(String name){
        Iterator<Field> iter = this.fields.iterator();
        while(iter.hasNext()){
            Field field = iter.next();
            if(field.getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        
        return false;
    }
    
    public boolean validate(){
        try{
            this.tableJsonSchema.validate(this.getJson());
            return true;
        }catch(ValidationException ve){
            return false;
        }
    }
    
    public JSONObject getJson(){
        //FIXME: Maybe we should use JSON serializer like Gson?
        JSONObject schemaJson = new JSONObject();
        schemaJson.put("fields", new JSONArray());
        
        for(Field field : fields) {
            schemaJson.getJSONArray("fields").put(field.getJson());   
        }
        
        return schemaJson;
    }
    
    public Object[] castRow(String[] row) throws InvalidCastException{
        
        if(row.length != this.fields.size()){
            throw new InvalidCastException("Row length is not equal to the number of defined fields.");
        }
        
        try{
            Object[] castRow = new Object[this.fields.size()];
        
            for(int i=0; i<row.length; i++){
                Field field = this.fields.get(i);

                String castMethodName = "cast" + (field.getType().substring(0, 1).toUpperCase() + field.getType().substring(1));;
                Method method = TypeInferrer.class.getMethod(castMethodName, String.class, String.class);

                castRow[i] = method.invoke(TypeInferrer.getInstance(), field.getFormat(), row[i]);
            }

            return castRow;
            
        }catch(Exception e){
            throw new InvalidCastException();
        }
        
    }
    
    public void write(String outputFilePath) throws IOException{
        try (FileWriter file = new FileWriter(outputFilePath)) {
            file.write(this.getJson().toString());
        }
    }
    
    public boolean hasFields(){
        return !this.getFields().isEmpty();
    }
   
}
