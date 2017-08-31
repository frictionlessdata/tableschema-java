package io.frictionlessdata.tableschema;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * 
 */
public class TableTest {
    
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
        
        // Fetch the data and apply the schema
        String employeeDataSourceFile = TableTest.class.getResource("/fixtures/employee_data.csv").getPath();
        Table employeeTable = new Table(employeeDataSourceFile, schema);
        
        // We will iterate the rows and these are the values classes we expect:
        Class[] expectedTypes = new Class[]{
            Integer.class,
            String.class,
            DateTime.class,
            Boolean.class,
            Integer[].class,
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
}
