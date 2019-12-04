package io.frictionlessdata.tableschema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DocumentationCases {



    //https://github.com/frictionlessdata/tableschema-java#parse-a-csv-without-a-schema
    @Test
    @DisplayName("Create AnyField")
    void testAnyFieldCreation() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master" +
                "/src/test/resources/fixtures/data/simple_data.csv");
        Table table = new Table(url);

        // Iterate through rows
        Iterator<Object[]> iter = table.iterator();
        while(iter.hasNext()){
            Object[] row = iter.next();
            System.out.println(Arrays.toString(row));
        }

        // [1, foo]
        // [2, bar]
        // [3, baz]

        // Read the entire CSV and output it as a List:
        List<Object[]> allData = table.read();
    }
}

