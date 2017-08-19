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
            URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
            Table table = new Table(url);
            
            Iterator<String[]> iter = table.iterator();
            while(iter.hasNext()){
                String[] row = iter.next();
                System.out.println(Arrays.toString(row));
            }
            
            List<String[]> allData = table.read();
            
            JSONObject schema = table.inferSchema();
            System.out.print(schema);
   
            

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
