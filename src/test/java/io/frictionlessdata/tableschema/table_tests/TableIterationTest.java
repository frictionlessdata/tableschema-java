package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.Field;
import io.frictionlessdata.tableschema.Schema;
import io.frictionlessdata.tableschema.Table;
import org.apache.commons.csv.CSVFormat;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 *
 * 
 */
public class TableIterationTest {
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

    private File getTestDataDirectory()throws Exception {
        URL u = TableIterationTest.class.getResource("/fixtures/simple_data.csv");
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