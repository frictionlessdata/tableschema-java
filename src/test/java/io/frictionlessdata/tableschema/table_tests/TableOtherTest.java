package io.frictionlessdata.tableschema.table_tests;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.frictionlessdata.tableschema.field.*;
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

/**
 *
 * 
 */
public class TableOtherTest {
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
    private static JSONObject populationSchema = new JSONObject("{\n" +
            "  \"fields\": [\n" +
            "    {\n" +
            "      \"name\": \"city\",\n" +
            "      \"format\": \"default\",\n" +
            "      \"description\": \"The city.\",\n" +
            "      \"type\": \"string\",\n" +
            "      \"title\": \"city\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"year\",\n" +
            "      \"description\": \"The year.\",\n" +
            "      \"type\": \"year\",\n" +
            "      \"title\": \"year\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"population\",\n" +
            "      \"description\": \"The population.\",\n" +
            "      \"type\": \"integer\",\n" +
            "      \"title\": \"population\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n");

    private static Object[][] populationTestData = new Object[][]
            {
                new Object[]{"london",2017,8780000},
                new Object[]{"paris",2017,2240000},
                new Object[]{"rome",2017,2860000}
            };

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testReadFromValidJSONArrayWithSchema() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = new Schema(schemaFile, true);
        Table table = new Table(populationTestJson.toString(), schema);

        Assert.assertEquals(3, table.read().size());
        Schema expectedSchema = new Schema (populationSchema.toString(), true);
        Table expectedTable = new Table(new File(getTestDataDirectory()
                , "data/population.csv")
                , getTestDataDirectory(), expectedSchema);
        Assert.assertEquals(expectedTable, table);
    }

    @Test
    public void testInferTypesIntAndDates() throws Exception{
        Table table = new Table(new File ("dates_data.csv"), getTestDataDirectory());
        
        JSONObject schema = table.inferSchema().getJson();
        JSONArray schemaFiles = schema.getJSONArray("fields");
        
        // The field names are the same as the name of the type we are expecting to be inferred.
        for(int i=0; i<schemaFiles.length(); i++){
            Assert.assertEquals(schemaFiles.getJSONObject(i).get("name"), schemaFiles.getJSONObject(i).get("type"));
        }
    }
    //TODO not sure how the test should correctly regard 1 as integer and as boolean at another time
    /*
    @Test
    public void testInferTypesIntBoolAndGeopoints() throws Exception{
        Table table = new Table(new File ("int_bool_geopoint_data.csv"), getTestDataDirectory());
        //String sourceFileAbsPath = TableOtherTest.class.getResource("/fixtures/int_bool_geopoint_data.csv").getPath();
        //Table table = new Table(sourceFileAbsPath);
        
        // Infer
        Schema schema = table.inferSchema();
        
        Iterator<Field> iter = schema.getFields().iterator();
        
        // The field names are the same as the name of the type we are expecting to be inferred.
        // So if type is set then in means that inferral worked.
        while(iter.hasNext()){
            Assert.assertEquals(iter.next().getName(), iter.next().getType());
        }
    }*/


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

            Assert.assertEquals(Long.class, row.get("id").getClass());
            Assert.assertEquals(String.class, row.get("name").getClass());
            Assert.assertEquals(DateTime.class, row.get("dateOfBirth").getClass());
            Assert.assertEquals(Boolean.class, row.get("isAdmin").getClass());
            Assert.assertEquals(int[].class, row.get("addressCoordinates").getClass());
            Assert.assertEquals(Duration.class, row.get("contractLength").getClass());
            Assert.assertEquals(JSONObject.class, row.get("info").getClass());      
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
            Long.class,
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
        URL u = TableOtherTest.class.getResource("/fixtures/simple_data.csv");
        Path path = Paths.get(u.toURI());
        return path.getParent().toFile();
    }

    private Schema getEmployeeTableSchema(){
        Schema schema = new Schema();

        Field idField = new IntegerField("id");
        schema.addField(idField);

        Field nameField = new StringField("name");
        schema.addField(nameField);

        Field dobField = new DateField("dateOfBirth");
        schema.addField(dobField);

        Field isAdminField = new BooleanField("isAdmin");
        schema.addField(isAdminField);

        Field addressCoordinatesField = new GeoPointField("addressCoordinatesField", Field.FIELD_FORMAT_OBJECT, null, null, null);
        schema.addField(addressCoordinatesField);

        Field contractLengthField = new DurationField("contractLength");
        schema.addField(contractLengthField);

        Field infoField = new ObjectField("info");
        schema.addField(infoField);

        return schema;
    }
}