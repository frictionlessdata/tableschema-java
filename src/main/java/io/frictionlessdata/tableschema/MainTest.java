/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema;

import java.util.Arrays;
import java.util.Iterator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author pechorin
 */
public class MainTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String sourceFileAbsPath = MainTest.class.getResource("/fixtures/int_string_bool_geopoint_data.csv").getPath();
        try{
            Table table = new Table(sourceFileAbsPath);
            
            /**
            System.out.println(Arrays.toString(table.headers()));
            
            //System.out.println(table.read());
            Iterator<String[]> iter1 = table.iterator();
            while (iter1.hasNext()) {
                System.out.println(Arrays.toString(iter1.next()));
            }
  
            Iterator<String[]> iter2 = table.iterator();
            while (iter2.hasNext()) {
                System.out.println(Arrays.toString(iter2.next()));
            }

            System.out.println(table.read());
            System.out.println(table.read());**/
            
            //table.inferSchema();
            
            TypeInferer typeInferer = new TypeInferer();
            String validDatetimeString = "2008-08-25T01:45:36.123Z";
            DateTime dt = typeInferer.castDatetime("default", validDatetimeString);
            
            System.out.println(dt.withZone(DateTimeZone.UTC).toString());

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
