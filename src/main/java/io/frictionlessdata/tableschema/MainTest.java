/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
            **/
            
         
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
