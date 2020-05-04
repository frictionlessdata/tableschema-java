package io.frictionlessdata.tableschema.table_tests;

import io.frictionlessdata.tableschema.datasourceformat.DataSourceFormat;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.Table;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

public class TableIterationTest {
/*     private static JSONArray populationTestJson =  new JSONArray("[" +
 *              "{" +
 *              "\"city\": \"london\"," +
 *              "\"year\": 2017," +
 *              "\"population\": 8780000" +
 *              "}," +
 *              "{" +
 *              "\"city\": \"paris\"," +
 *              "\"year\": 2017," +
 *              "\"population\": 2240000" +
 *              "}," +
 *              "{" +
 *              "\"city\": \"rome\"," +
 *              "\"year\": 2017," +
 *              "\"population\": 2860000" +
 *              "}" +
 *              "]");
 *
 *     private static Object[][] populationTestData = new Object[][]
 *             {
 *                 new Object[]{"london",2017,8780000},
 *                 new Object[]{"paris",2017,2240000},
 *                 new Object[]{"rome",2017,2860000}
 *             }; */

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
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assert.assertEquals("[id, title]", Arrays.toString(table.getHeaders()));
    }

    @Test
    public void testReadUncastData() throws Exception{
        File testDataDir = getTestDataDirectory();
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assert.assertEquals(3, table.read().size());
        Assert.assertEquals("1", table.read().get(0)[0]);
        Assert.assertEquals("foo", table.read().get(0)[1]);
    }

    @Test
    public void testReadCastDataWithSchema() throws Exception{
        File testDataDir = getTestDataDirectory();

        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();

        // Fetch the data and apply the schema
        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir, employeeTableSchema, DataSourceFormat.getDefaultCsvFormat());

        // We will iterate the rows and these are the values classes we expect:
        Class[] expectedTypes = new Class[]{
            BigInteger.class,
            String.class,
            LocalDate.class,
            Boolean.class,
            double[].class,
            Duration.class,
            HashMap.class
        };

        List<Object[]> data = employeeTable.read(true);
        Iterator<Object[]> iter = data.iterator();

        while(iter.hasNext()){
            Object[] row = iter.next();

            for(int i=0; i<row.length; i++){
                Assert.assertTrue(expectedTypes[i].isAssignableFrom(row[i].getClass()));
            }
        }
    }


    @Test
    public void testReadExtendedDataWithSchema() throws Exception{
        File testDataDir = getTestDataDirectory();
        Schema employeeTableSchema = getEmployeeTableSchema();

        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir, employeeTableSchema, null);

        Iterator iter = employeeTable.iterator(false, true, false, false);

        String referenceContent =
                String.join("", Files.readAllLines(new File(testDataDir, "data/employee_data_string.json").toPath()));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode referenceArr = objectMapper.readTree(referenceContent);

        int i = 0;

        while(iter.hasNext()){
            Object[] row = (Object[])iter.next();
            JsonNode reference = referenceArr.get(i);

            Assert.assertEquals(3, row.length);
            Assert.assertEquals(i, row[0]);

            String[] keys = (String[]) row[1];

            for (int j = 0; j < keys.length; j++) {
                String key = keys[j];
                Object val = ((Object[])row[2])[j];
                if (val instanceof Boolean) {
                    Assert.assertEquals(reference.get(key).asText().equals("true"), val);
                } else if (val instanceof double[]){
                    JsonNode objVal = objectMapper.readTree(reference.get(key).textValue());
                    double lon = objVal.get("lon").asDouble();
                    double valDouble = ((double[]) val)[0];
                    double delta = 0.05;
                    Assert.assertEquals(lon, valDouble, delta);
                } else if (val instanceof Duration) {
                    Duration testDur = Duration.parse(reference.get(key).textValue());
                    Assert.assertEquals(testDur, ((Duration)val));
                } else if (val instanceof Map) {
                    JsonNode objVal = objectMapper.readTree(reference.get(key).textValue());
                    for (Object k : ((Map)val).keySet()) {
                        Object v = ((Map)val).get(k).toString();
                        Assert.assertEquals(objVal.get((String)k).toString(), v);
                    }
                } else {
                    Assert.assertEquals(reference.get(key).asText(), val.toString());
                }
            }
            i++;
        }
    }

    @Test
    public void testReadKeyedDataWithoutSchema() throws Exception{
        File testDataDir = getTestDataDirectory();

        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir);

        Iterator iter = employeeTable.iterator(true, false, false, false);

        String referenceContent =
                String.join("", Files.readAllLines(new File(testDataDir, "data/employee_data_string.json").toPath()));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode referenceArr = objectMapper.readTree(referenceContent);

        int i = 0;

        while(iter.hasNext()){
            Map<String, Object> row = (Map<String, Object>)iter.next();
            JsonNode reference = referenceArr.get(i);

            Assert.assertEquals(7, row.size());

            for(String key : row.keySet()){
                Object val = row.get(key);
                Assert.assertEquals(reference.get(key).textValue(), val);
            }
            i++;
        }
    }

    @Test
    public void testReadExtendedDataWithoutSchema() throws Exception{
        File testDataDir = getTestDataDirectory();

        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir);

        Iterator iter = employeeTable.iterator(false, true, false, false);

        String referenceContent =
                String.join("", Files.readAllLines(new File(testDataDir, "data/employee_data_string.json").toPath()));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode referenceArr = objectMapper.readTree(referenceContent);

        int i = 0;

        while(iter.hasNext()){
            Object[] row = (Object[])iter.next();
            JsonNode reference = referenceArr.get(i);

            Assert.assertEquals(3, row.length);
            Assert.assertEquals(i, row[0]);

            String[] keys = (String[])row[1];
            for (int j = 0; j < keys.length; j++) {
                String key = keys[j];
                String val = ((String[])row[2])[j];
                Assert.assertEquals(reference.get(key).textValue(), val);
            }
            i++;
        }
    }

    /*
    Reading from a CR/LF CSV can switch the `population` property to string
    as it has a trailing CR unless stripped
     */
    @Test
    public void testReadFromValidFileWithCRLF() throws Exception{
        // get path of test CSV file
        URL sourceFileUrl = TableCreationTest.class.getResource("/fixtures/data/population.csv");
        Path path = Paths.get(sourceFileUrl.toURI());
        String csvContent = new String(Files.readAllBytes(path));

        File f = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = null;
        try (FileInputStream fis = new FileInputStream(f)) {
            schema = Schema.fromJson (fis, false);
        }

        Table table = Table.fromSource(csvContent, schema, DataSourceFormat.getDefaultCsvFormat());

        Assert.assertEquals(3, table.read().size());
        List<Object[]> actualData = table.read(true);
        for (int i = 0; i < actualData.size(); i++) {
            Assert.assertTrue("Expected Number for population figures, CR/LF problem"
                    , actualData.get(i)[2] instanceof Number);
        }
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

        Field addressCoordinatesField
                = new GeopointField("addressCoordinates", Field.FIELD_FORMAT_OBJECT, null, null, null, null, null);
        schema.addField(addressCoordinatesField);

        Field contractLengthField = new DurationField("contractLength");
        schema.addField(contractLengthField);

        Field infoField = new ObjectField("info");
        schema.addField(infoField);

        return schema;
    }
}
