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
import org.joda.time.DateTime;
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
            
            Schema schema = new Schema();
        
            Field idField = new Field("id", Field.FIELD_TYPE_INTEGER);
            schema.addField(idField);

            Field nameField = new Field("name", Field.FIELD_TYPE_STRING);
            schema.addField(nameField);

            Field dobField = new Field("dateOfBirth", Field.FIELD_TYPE_DATE); 
            schema.addField(dobField);

            Field isAdminField = new Field("isAdmin", Field.FIELD_TYPE_BOOLEAN);
            schema.addField(isAdminField);
            
            Field addressCoordinatesField = new Field("addressCoordinatesField", Field.FIELD_TYPE_GEOPOINT, Field.FIELD_FORMAT_OBJECT);
            schema.addField(addressCoordinatesField);

            Field contractLengthField = new Field("contractLength", Field.FIELD_TYPE_DURATION);
            schema.addField(contractLengthField);

            Field infoField = new Field("info", Field.FIELD_TYPE_OBJECT);
            schema.addField(infoField);
           
            sourceFileAbsPath = MainTest.class.getResource("/fixtures/employee_data.csv").getPath();
            Table table = new Table(sourceFileAbsPath, schema);
            
            TableIterator<Object[]> iter = table.iterator();
            while(iter.hasNext()){

                // The fetched array will contain row values that have been cast into their
                // appropriate types as per field definitions in the schema.
                Object[] row = iter.next();

                int id = (int)row[0];
                String name = (String)row[1];
                DateTime dob = (DateTime)row[2];
                boolean isAdmin = (boolean)row[3];
                int[] addressCoordinates = (int[])row[4];
                Duration contractLength = (Duration)row[5];
                JSONObject info = (JSONObject)row[6];
            }
            
             /**Iterator<Object[]> iter = table.iterator();
            while(iter.hasNext()){
                Object[] row = iter.next();
                
               
                for(int i=0; i<row.length; i++){
                    System.out.print(" " + row[i].getClass());
                }
                System.out.println("");
                
                System.out.println(row[0].getClass());
                int id = (int)row[0];
                
                System.out.println(row[1].getClass());
                String name = (String)row[1];
                
                System.out.println(row[2].getClass());
                DateTime dob = (DateTime)row[2];
                boolean isAdmin = (boolean)row[3];
                int[] coords = (int[])row[4];
                Duration contractLength = (Duration)row[5];
                JSONObject info = (JSONObject)row[6];
            }
            **/
            

            // Iterate table
            /**
            // Create table from URL
            URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
            Table table = new Table(url);
            
            Iterator<Object[]> iter = table.iterator();
            while(iter.hasNext()){
                Object[] row = iter.next();
                System.out.println(Arrays.toString(row));
            }**/
            /**
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
            
            /**
            Map<String, Object> violatedConstraints = null;
        
            Map<String, Object> constraints = new HashMap();
            constraints.put(Field.CONSTRAINT_KEY_MIN_LENGTH, 36);
            constraints.put(Field.CONSTRAINT_KEY_MAX_LENGTH, 45);

            Field field = new Field("test", Field.FIELD_TYPE_STRING, null, null, null, constraints);
            field.castValue("This string length is greater than 45 characters.");
            **/
            
            /**
            Map<String, Object> constraints = new HashMap();
            constraints.put(Field.CONSTRAINT_KEY_MINIMUM, 5);
            constraints.put(Field.CONSTRAINT_KEY_MAXIMUM, 15);

            Field field = new Field("name", Field.FIELD_TYPE_INTEGER, null, null, null, constraints);

            int constraintViolatingValue = 16;
            Map<String, Object> violatedConstraints = field.checkConstraintViolations(constraintViolatingValue);

            System.out.println(violatedConstraints);
            **/
            
            /**
            Schema schema = new Schema();
        
            Field fieldString = new Field("name", Field.FIELD_TYPE_STRING);
            schema.addField(fieldString);

            Field fieldInteger = new Field("id", Field.FIELD_TYPE_INTEGER);
            schema.addField(fieldInteger);

            Field fieldBoolean = new Field("isAdmin", Field.FIELD_TYPE_BOOLEAN);
            schema.addField(fieldBoolean);

            String[] row = new String[]{"John Doe", "25", "True"};

            Object[] castRow = schema.castRow(row);
            
            System.out.println(Arrays.asList(castRow));
            **/
            /**
            String sf = MainTest.class.getResource("/fixtures/int_bool_geopoint_data.csv").getPath();
            Table table = new Table(sf);

            table.inferSchema().getJson();


            // Fetch the data and apply the schema
            sourceFileAbsPath = MainTest.class.getResource("/fixtures/employee_data.csv").getPath();
            Table employeeTable = new Table(sourceFileAbsPath, schema);

            // We will iterate the rows and these are the values classes we expect:
            Class[] expectedTypes = new Class[]{
                Integer.class,
                String.class,
                DateTime.class,
                Boolean.class,
                int[].class,
                Duration.class,
                JSONObject.class
            };

            System.out.println("CASTED ROW:");        
            TableIterator<Object[]> iter = employeeTable.iterator();

            while(iter.hasNext()){
                Object[] row = iter.next();
                System.out.println(Arrays.asList(row));
            }
            System.out.println("");
            
            
            System.out.println("\n\nCASTED ROW AND KEYED:");            
            TableIterator<Map> iter2 = employeeTable.iterator(true);

            while(iter2.hasNext()){
                Map row = iter2.next();

                Iterator<Map.Entry<String, Object>> it = row.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> pair = it.next();
                    System.out.println(pair);
                }
                System.out.println("");
            }
            
            System.out.println("CASTED ROW AND EXTENDED:");
            TableIterator<Object[]> iter3 = employeeTable.iterator(false, true);

            while(iter3.hasNext()){
                Object[] row = iter3.next();
                System.out.println(Arrays.asList(row));
            }
            System.out.println("");
            **/
            
            /**
            Field intField = new Field("intNum", Field.FIELD_TYPE_NUMBER);
            Field floatField = new Field("floatNum", Field.FIELD_TYPE_NUMBER);
            
            float floatValPositive1 = floatField.castValue("123.9902");
            System.out.println(floatValPositive1);
            **/
            
            Map<String, Object> options = new HashMap();
            options.put("decimalChar", ",");
            float num = (float)TypeInferrer.getInstance().castNumber(Field.FIELD_FORMAT_DEFAULT, "1020,123", options);

            System.out.println(num);
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
