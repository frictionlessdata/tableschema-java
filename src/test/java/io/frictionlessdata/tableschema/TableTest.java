package io.frictionlessdata.tableschema;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * 
 */
public class TableTest {
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
                new Object[]{"london",2017,8780000},
                new Object[]{"paris",2017,2240000},
                new Object[]{"rome",2017,2860000}
            };

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void testReadFromValidFilePath() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("simple_data.csv");
        Table table = new Table(file, testDataDir);
        
        Assert.assertEquals(3, table.read().size()); 
    }
    
    @Test
    public void testReadFromValidCSVContentString() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableTest.class.getResource("/fixtures/simple_data.csv");
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
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
        Table table = new Table(url);
        
        Assert.assertEquals(3, table.read().size());
    }


    @Test
    public void testReadFromValidFileWithValidSchema() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableTest.class.getResource("/fixtures/data/population.csv");
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

    //FIXME: Too slow.
    /**
    @Test
    public void testInferTypesIntAndDates() throws Exception{
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/dates_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        JSONObject schema = table.inferSchema().getJson();
        JSONArray schemaFiles = schema.getJSONArray("fields");
        
        // The field names are the same as the name of the type we are expecting to be inferred.
        for(int i=0; i<schemaFiles.length(); i++){
            Assert.assertEquals(schemaFiles.getJSONObject(i).get("name"), schemaFiles.getJSONObject(i).get("type"));
        }
    }
    **/
    
    //FIXME: Too slow.
    /**
    @Test
    public void testInferTypesIntBoolAndGeopoints() throws Exception{
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/int_bool_geopoint_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        // Infer
        Schema schema = table.inferSchema();
        
        Iterator<Field> iter = schema.getFields().iterator();
        
        // The field names are the same as the name of the type we are expecting to be inferred.
        // So if type is set then in means that inferral worked.
        while(iter.hasNext()){
            Assert.assertEquals(iter.next().getName(), iter.next().getType());
        }
    }**/
    
    @Test
    public void testIterateUncastData() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("simple_data.csv");
        Table table = new Table(file, testDataDir);
        
        List<String[]> expectedResults = new ArrayList();
        expectedResults.add(new String[]{"1", "foo"});
        expectedResults.add(new String[]{"2", "bar"});
        expectedResults.add(new String[]{"3", "baz"});

        Iterator<Object[]> iter = table.iterator();
        int loopCounter = 0;
        while (iter.hasNext()) {
            Object[] row = iter.next();
            Assert.assertEquals(expectedResults.get(loopCounter)[0], row[0]);
            Assert.assertEquals(expectedResults.get(loopCounter)[1], row[1]);
            loopCounter++;
        }
    }

    
    @Test
    public void testIterateUncastKeyedData() throws Exception{
        File testDataDir = getTestDataDirectory();
        // Fetch the data
        File file = new File("employee_data.csv");
        Table employeeTable = new Table(file, testDataDir);
        
        Iterator<Map> iter = employeeTable.iterator(true);

        while(iter.hasNext()){
            Map row = iter.next();

            Assert.assertEquals(String.class, row.get("id").getClass());
            Assert.assertEquals(String.class, row.get("name").getClass());
            Assert.assertEquals(String.class, row.get("dateOfBirth").getClass());
            Assert.assertEquals(String.class, row.get("isAdmin").getClass());
            Assert.assertEquals(String.class, row.get("addressCoordinates").getClass());
            Assert.assertEquals(String.class, row.get("contractLength").getClass());
            Assert.assertEquals(String.class, row.get("info").getClass());     
        }
    }
    
    @Test
    public void testIterateUncastExtendedData() throws Exception{
        File testDataDir = getTestDataDirectory();
        // Fetch the data.

        File file = new File("employee_data.csv");
        Table employeeTable = new Table(file, testDataDir);
   
        Iterator<Object[]> iter = employeeTable.iterator(false, true, false, false);
        
        int rowIndex = 0;
        while(iter.hasNext()){
            Object[] row = iter.next();

            Assert.assertEquals(rowIndex, row[0]);
            Assert.assertArrayEquals(employeeTable.getHeaders(), (String[])row[1]);
           
            Object[] dataArray = (Object[])row[2];
            Assert.assertEquals(String.class, dataArray[0].getClass());
            Assert.assertEquals(String.class, dataArray[1].getClass());
            Assert.assertEquals(String.class, dataArray[2].getClass());
            Assert.assertEquals(String.class, dataArray[3].getClass());
            Assert.assertEquals(String.class, dataArray[4].getClass());
            Assert.assertEquals(String.class, dataArray[5].getClass());
            Assert.assertEquals(String.class, dataArray[6].getClass());
            
            rowIndex++;
        }
    }
    
    @Test
    public void testIterateCastData() throws Exception{
        File testDataDir = getTestDataDirectory();
        
        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();
        
        // Fetch the data and apply the schema

        File file = new File("employee_data.csv");
        Table employeeTable = new Table(file, testDataDir, employeeTableSchema);
        
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
        
        // Let's iterate and assert row value classes against the expected classes
        Iterator<Object[]> iter = employeeTable.iterator();
        
        while (iter.hasNext()) {
            Object[] row = iter.next();
            for(int i=0; i<row.length; i++){
                Assert.assertEquals(expectedTypes[i], row[i].getClass());
            }
        }
  
    }
    
    @Test
    public void testIterateCastKeyedData() throws Exception{
        File testDataDir = getTestDataDirectory();
        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();
        
        // Fetch the data and apply the schema
        File file = new File("employee_data.csv");
        Table employeeTable = new Table(file, testDataDir, employeeTableSchema);
        
        Iterator<Map> iter = employeeTable.iterator(true, false, false, false);

        while(iter.hasNext()){
            Map row = iter.next();

            Assert.assertEquals(Integer.class, row.get("id").getClass());
            Assert.assertEquals(String.class, row.get("name").getClass());
            Assert.assertEquals(DateTime.class, row.get("dateOfBirth").getClass());
            Assert.assertEquals(Boolean.class, row.get("isAdmin").getClass());
            Assert.assertEquals(int[].class, row.get("addressCoordinates").getClass());
            Assert.assertEquals(Duration.class, row.get("contractLength").getClass());
            Assert.assertEquals(JSONObject.class, row.get("info").getClass());      
        }
    }
    
    @Test
    public void testIterateCastExtendedData() throws Exception{
        File testDataDir = getTestDataDirectory();
        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();
        
        // Fetch the data and apply the schema
        File file = new File("employee_data.csv");
        Table employeeTable = new Table(file, testDataDir, employeeTableSchema);
        
        Iterator<Object[]> iter = employeeTable.iterator(false, true, false, false);
        
        int rowIndex = 0;
        while(iter.hasNext()){
            Object[] row = iter.next();

            Assert.assertEquals(rowIndex, row[0]);
            Assert.assertArrayEquals(employeeTable.getHeaders(), (String[])row[1]);
           
            Object[] dataArray = (Object[])row[2];
            Assert.assertEquals(Integer.class, dataArray[0].getClass());
            Assert.assertEquals(String.class, dataArray[1].getClass());
            Assert.assertEquals(DateTime.class, dataArray[2].getClass());
            Assert.assertEquals(Boolean.class, dataArray[3].getClass());
            Assert.assertEquals(int[].class, dataArray[4].getClass());
            Assert.assertEquals(Duration.class, dataArray[5].getClass());
            Assert.assertEquals(JSONObject.class, dataArray[6].getClass());
            
            rowIndex++;
        }
    }
    
    @Test
    public void testFetchHeaders() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("simple_data.csv");
        Table table = new Table(file, testDataDir);
        
        Assert.assertEquals("[id, title]", Arrays.toString(table.getHeaders()));
    }
    
    @Test
    public void testReadUncastData() throws Exception{
        File testDataDir = getTestDataDirectory();
        File file = new File("simple_data.csv");
        Table table = new Table(file, testDataDir);
        
        Assert.assertEquals(3, table.read().size());
        Assert.assertEquals("1", table.read().get(0)[0]);
        Assert.assertEquals("foo", table.read().get(0)[1]);
    }

    @Test
    public void testReadCastData() throws Exception{
        File testDataDir = getTestDataDirectory();

        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();
        
        // Fetch the data and apply the schema
        File file = new File("employee_data.csv");
        Table employeeTable = new Table(file, testDataDir, employeeTableSchema);
        
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
        
        List<Object[]> data = employeeTable.read(true);
        Iterator<Object[]> iter = data.iterator();
        
        while(iter.hasNext()){
            Object[] row = iter.next();
            
            for(int i=0; i<row.length; i++){
                Assert.assertEquals(expectedTypes[i], row[i].getClass());
            }
        }
    }
    
    @Test
    public void saveTable() throws Exception{
        String createdFileName = "test_data_table.csv";
        File createdFileDir = folder.newFile(createdFileName).getParentFile();
        File testDataDir = getTestDataDirectory();
        File file = new File("simple_data.csv");
        Table loadedTable = new Table(file, testDataDir);
        
        loadedTable.writeCsv(new File (createdFileDir, createdFileName), CSVFormat.RFC4180);
        
        Table readTable = new Table(new File(createdFileName), createdFileDir);
        Assert.assertEquals("id", readTable.getHeaders()[0]);
        Assert.assertEquals("title", readTable.getHeaders()[1]);
        Assert.assertEquals(3, readTable.read().size());   
    }

    private File getTestDataDirectory()throws Exception {
        URL u = TableTest.class.getResource("/fixtures/simple_data.csv");
        Path path = Paths.get(u.toURI());
        return path.getParent().toFile();
    }
    
    private Schema getEmployeeTableSchema(){
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
        
        return schema;
    }
}