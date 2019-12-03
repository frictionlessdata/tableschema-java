package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.Schema;
import io.frictionlessdata.tableschema.Table;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

/**
 *
 * 
 */
public class TableCreationTest {
    private static JSONArray populationTestJson =  new JSONArray("[" +
             "{" +
             "\"city\": \"london\"," +
             "\"year\": 2017," +
             "\"population\": 8780000" +
             "}," +
             "{" +
             "\"city\": \"paris\"," +
             "\"year\": 2017," +
             "\"population\": 2240000" +
             "}," +
             "{" +
             "\"city\": \"rome\"," +
             "\"year\": 2017," +
             "\"population\": 2860000" +
             "}" +
             "]");

    private static Object[][] populationTestData = new Object[][]
            {
                new Object[]{"london",2017,8780000L},
                new Object[]{"paris",2017,2240000L},
                new Object[]{"rome",2017,2860000L}
            };

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void testReadFromValidFilePath() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data.csv");
        Table table = new Table(file, testDataDir);
        
        Assert.assertEquals(3, table.read().size()); 
    }
    
    @Test
    public void testReadFromValidCSVContentString() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/simple_data.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));
        
        Table table = new Table(csvContent, null);
        
        Assert.assertEquals(3, table.read().size()); 
    }
    
    @Test
    public void testReadFromValidJSONArray() throws Exception{
        Table table = new Table(populationTestJson.toString());
        Assert.assertEquals(3, table.read().size());
        Schema schema = table.inferSchema();
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema expectedSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = new Schema(fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field expectedField = expectedSchema.getFields().get(i);
                Field actualField = schema.getFields().get(i);
                Assert.assertEquals(expectedField,actualField);
            }
        }
        Assert.assertEquals(expectedSchema, schema);
    }


    @Test
    public void testReadFromValidJSONArrayWithSchema() throws Exception{
        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        Schema schema = new Schema(f, true);
        Table table = new Table(populationTestJson.toString(), schema);
        Assert.assertEquals(3, table.read().size());
        Schema expectedSchema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            expectedSchema = new Schema(fis, false);
        }

        if (!expectedSchema.equals(schema)) {
            for (int i = 0; i < expectedSchema.getFields().size(); i++) {
                Field expectedField = expectedSchema.getFields().get(i);
                Field actualField = schema.getFields().get(i);
                Assert.assertEquals(expectedField,actualField);
            }
        }
        Assert.assertEquals(expectedSchema, schema);
    }
    
    @Test
    public void testReadFromValidUrl() throws Exception{
        // get path of test CSV file
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
        Table table = new Table(url);
        
        Assert.assertEquals(3, table.read().size());
    }


    @Test
    public void testReadFromValidUrlAndValidSchemaURL() throws Exception{
        // get path of test CSV file
        URL tableUrl = new URL("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/data/population.csv");
        URL schemaUrl = new URL("https://raw.githubusercontent.com/frictionlessdata" +
                "/tableschema-java/master/src/test/resources/fixtures/schema/population_schema.json");
        Table table = new Table(tableUrl, schemaUrl);

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = new Schema(schemaFile, true);
        Table testTable = new Table(populationTestJson.toString(), testSchema);

        Assert.assertEquals(testTable, table);
    }



    @Test
    public void testReadFromValidFileWithValidSchema() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = new Schema(fis, false);
        }

        Table table = new Table(csvContent, schema);

        Assert.assertEquals(3, table.read().size());
        List<Object[]> actualData = table.read();
        for (int i = 0; i < actualData.size(); i++) {
            Object[] actualRow = actualData.get(i);
            Object[] testRow = populationTestData[i];
            Assert.assertArrayEquals(testRow, actualRow);
        }
    }


    @Test
    public void testReadFromValidFileWithValidSchemaViaStream() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");

        ByteArrayInputStream bis = new ByteArrayInputStream(csvContent.getBytes());
        FileInputStream fis = new FileInputStream(f);
        Table table = new Table(bis, fis);

        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema testSchema = new Schema(schemaFile, true);
        Table testTable = new Table(populationTestJson.toString(), testSchema);
        Assert.assertEquals(testTable, table);
        try {
            bis.close();
        } finally {
            fis.close();
        }
    }

}