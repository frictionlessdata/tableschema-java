package io.frictionlessdata.tableschema;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void testReadFromValidFilePath() throws Exception{
        // get path of test CSV file
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        Assert.assertEquals(3, table.read().size()); 
    }
    
    @Test
    public void testReadFromValidUrl() throws Exception{
        // get path of test CSV file
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
        Table table = new Table(url);
        
        Assert.assertEquals(3, table.read().size());
    }
    
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
    
    @Test
    public void testInferTypesIntBoolAndGeopoints() throws Exception{
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/int_bool_geopoint_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        JSONObject schema = table.inferSchema().getJson();
        JSONArray schemaFiles = schema.getJSONArray("fields");
        
        // The field names are the same as the name of the type we are expecting to be inferred.
        for(int i=0; i<schemaFiles.length(); i++){
            Assert.assertEquals(schemaFiles.getJSONObject(i).get("name"), schemaFiles.getJSONObject(i).get("type"));
        }
    }
    
    @Test
    public void testIterate() throws Exception{
        // get path of test CSV file
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        String[] expectedResults = new String[]{"[1, foo]", "[2, bar]", "[3, baz]"};
        
        Iterator<Object[]> iter = table.iterator();
        int loopCounter = 0;
        while (iter.hasNext()) {
            String stringifiedRow = Arrays.toString(iter.next());
            Assert.assertEquals(expectedResults[loopCounter], stringifiedRow);
            loopCounter++;
        }
    }
    
    @Test
    public void testIterateTableWithSchema() throws Exception{
        
        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();
        
        // Fetch the data and apply the schema
        String employeeDataSourceFile = TableTest.class.getResource("/fixtures/employee_data.csv").getPath();
        Table employeeTable = new Table(employeeDataSourceFile, employeeTableSchema);
        
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
    public void testFetchHeaders() throws Exception{
        // get path of test CSV file
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        Assert.assertEquals("[id, title]", Arrays.toString(table.headers()));
    }
    
    @Test
    public void testReadUncastedData() throws Exception{
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        Assert.assertEquals(3, table.read().size());
    }
    
    @Test
    public void testReadCastedData() throws Exception{

        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();
        
        // Fetch the data and apply the schema
        String employeeDataSourceFile = TableTest.class.getResource("/fixtures/employee_data.csv").getPath();
        Table employeeTable = new Table(employeeDataSourceFile, employeeTableSchema);
        
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
    public void writeTable() throws Exception{
        File createdFile = folder.newFile("test_data_table.csv");
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table loadedTable = new Table(sourceFileAbsPath);
        
        loadedTable.write(createdFile.getAbsolutePath());
        
        Table readTable = new Table(createdFile.getAbsolutePath());
        Assert.assertEquals(loadedTable.headers()[0], "id");
        Assert.assertEquals(loadedTable.headers()[1], "title");
        Assert.assertEquals(loadedTable.read().size(), readTable.read().size());   
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
