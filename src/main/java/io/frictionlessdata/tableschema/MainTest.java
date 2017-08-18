/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.frictionlessdata.tableschema;

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
            Table table = new Table(sourceFileAbsPath);
            
            System.out.println(table.inferSchema().toString());
            

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
