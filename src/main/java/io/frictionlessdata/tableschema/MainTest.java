/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author pechorin
 */
public class MainTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String sourceFileAbsPath = MainTest.class.getResource("/fixtures/dates_data.csv").getPath();
        try{
            // Creat table from URL
            URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
            Table table = new Table(url);
            
            /**
            // Iterate table
            Iterator<String[]> iter = table.iterator();
            while(iter.hasNext()){
                String[] row = iter.next();
                //System.out.println(Arrays.toString(row));
            }
            
            // Load entire table
            List<String[]> allData = table.read();
            
            // Infer Schema
            JSONObject schemaJsonObj = table.inferSchema();
            //System.out.print(schemaJsonObj);
   
            // Build Schema with Field instances
            Schema schema = new Schema();

            Field nameField = new Field("name", "string");
            schema.addField(nameField);
            
            Field coordinatesField = new Field("coordinates", "geopoint");
            schema.addField(coordinatesField);
            
            System.out.println(schema.getJson());
            **/
            
            // Build Schema with JSONObject instances

            /**
            Schema schema2 = new Schema();
            
            JSONObject nameFieldJsonObject = new JSONObject();
            nameFieldJsonObject.put("name", "name");
            nameFieldJsonObject.put("type", "string");
            schema2.addField(nameFieldJsonObject);
            
            // An invalid Field definition, will be ignored.
            JSONObject invalidFieldJsonObject = new JSONObject();
            invalidFieldJsonObject.put("name", "id");
            invalidFieldJsonObject.put("type", "integer");
            invalidFieldJsonObject.put("format", "invalid");
            schema2.addField(invalidFieldJsonObject);
            
            JSONObject coordinatesFieldJsonObject = new JSONObject();
            coordinatesFieldJsonObject.put("name", "coordinates");
            coordinatesFieldJsonObject.put("type", "geopoint");
            coordinatesFieldJsonObject.put("format", "array");
            schema2.addField(coordinatesFieldJsonObject);
            
            System.out.println(schema2.getJson());
            
  
            JSONObject schemaJsonObj3 = new JSONObject();
            Field nameField3 = new Field("id", "integer");
            schemaJsonObj3.put("fields", new JSONArray());
            schemaJsonObj3.getJSONArray("fields").put(nameField3.getJson());
            
            Schema schema3 = new Schema(schemaJsonObj3);
            
            boolean isValid = schema3.validate();
            System.out.println(isValid);
            
            Field invalidField3 = new Field("coordinates", "invalid");
            schemaJsonObj3.getJSONArray("fields").put(invalidField3.getJson());
            
            isValid = schema3.validate();
            System.out.println(isValid);
            **/
 
            
            /**
            Field field = new Field("name", "geopoint", "default");
            int[] val = field.castValue("12,21");
            System.out.print("YPYOOY: " + val[0]);
            
            
            Field field2 = new Field("name", "geopoint", "array");
            int[] val2 = field2.castValue("[12,21]");
            System.out.print(val2[0]);
            
            Field field3 = new Field("name", "geopoint", "object");
            int[] val3 = field3.castValue("{\"lon\": 12, \"lat\": 21}");
            System.out.print(val[30]);
   
            Field field4 = new Field("name", "duration");
            Duration val4 = field4.castValue("P2DT3H4M");
            System.out.print(val4.getSeconds());
            **/
            
            /**
            Map<String, Object> violatedConstraints = null;

            Map<String, Object> constraints = new HashMap();
            constraints.put("required", true);

            Field field = new Field("name", Field.FIELD_TYPE_STRING, null, null, null, constraints);
            String valueNotNull = field.castValue("This is a string value");
            
            System.out.println(valueNotNull);
            
            String valueNull = field.castValue(null); 
            violatedConstraints = field.checkConstraintViolations(valueNull);
            
            System.out.println(violatedConstraints.toString());
            ***/
            
            /**
            Map<String, Object> violatedConstraints = null;
        
            Map<String, Object> constraints = new HashMap();

            List<String> enumStrings = new ArrayList();
            enumStrings.add("one");
            enumStrings.add("two");
            enumStrings.add("four");

            constraints.put(Field.CONSTRAINT_KEY_ENUM, enumStrings);
        
            Field field = new Field("test", Field.FIELD_TYPE_STRING, null, null, null, constraints);
            violatedConstraints = field.checkConstraintViolations("three");
            System.out.println(violatedConstraints);
            
            violatedConstraints = field.checkConstraintViolations("two");
            System.out.println(violatedConstraints);
            **/
            
            /**
            Map<String, Object> violatedConstraints = null;
        
            Map<String, Object> constraints = new HashMap();
            List<JSONObject> enumObjs = new ArrayList();

            JSONObject obj1 = new JSONObject();
            obj1.put("one", 1);
            enumObjs.add(obj1);

            JSONObject obj2 = new JSONObject();
            obj1.put("one", 1);
            obj1.put("two", 2);
            enumObjs.add(obj2);

            JSONObject obj3 = new JSONObject();
            obj1.put("one", 1);
            obj1.put("two", 2);
            obj1.put("four", 4);
            enumObjs.add(obj3);
            
            constraints.put(Field.CONSTRAINT_KEY_ENUM, enumObjs);
            Field field = new Field("test", Field.FIELD_TYPE_OBJECT, null, null, null, constraints);     
            
            violatedConstraints = field.checkConstraintViolations(obj3);
            System.out.println(violatedConstraints);
            **/
            
            /**
            Map<String, Object> violatedConstraints = null;
        
            Map<String, Object> constraints = new HashMap();
            constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
            constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);
            
            Field field = new Field("test", Field.FIELD_TYPE_STRING, null, null, null, constraints);
            String valueLength35 = field.castValue("This string length is less than 36.");
            **/
            
            Map<String, Object> violatedConstraints = null;
        
            Map<String, Object> constraints = new HashMap();
            constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
            constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);

            Field field = new Field("test", Field.FIELD_TYPE_STRING, null, null, null, constraints);
            field.castValue("This string length is greater than 45 characters.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
