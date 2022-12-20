package io.frictionlessdata.tableschema.table_tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.tabledatasource.TableDataSource;
import io.frictionlessdata.tableschema.field.*;
import io.frictionlessdata.tableschema.schema.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

public class TableIterationTest {

    @Test
    public void testFetchHeaders() throws Exception{
        File testDataDir = getTestDataDirectory();
        // get path of test CSV file
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assertions.assertEquals("[id, title]", Arrays.toString(table.getHeaders()));
    }

    @Test
    public void testReadUncastData() throws Exception{
        File testDataDir = getTestDataDirectory();
        File file = new File("data/simple_data.csv");
        Table table = Table.fromSource(file, testDataDir);

        Assertions.assertEquals(3, table.read().size());
        Assertions.assertEquals("1", table.read().get(0)[0]);
        Assertions.assertEquals("foo", table.read().get(0)[1]);
    }

    @Test
    public void testReadCastDataWithSchema() throws Exception{
        File testDataDir = getTestDataDirectory();

        // Let's start by defining and building the schema:
        Schema employeeTableSchema = getEmployeeTableSchema();

        // Fetch the data and apply the schema
        File file = new File("data/employee_data.csv");
        Table employeeTable = Table.fromSource(file, testDataDir, employeeTableSchema, TableDataSource.getDefaultCsvFormat());

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
                Assertions.assertTrue(expectedTypes[i].isAssignableFrom(row[i].getClass()));
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

            Assertions.assertEquals(3, row.length);
            Assertions.assertEquals(i, row[0]);

            String[] keys = (String[]) row[1];

            for (int j = 0; j < keys.length; j++) {
                String key = keys[j];
                Object val = ((Object[])row[2])[j];
                if (val instanceof Boolean) {
                    Assertions.assertEquals(reference.get(key).asText().equals("true"), val);
                } else if (val instanceof double[]){
                    JsonNode objVal = objectMapper.readTree(reference.get(key).textValue());
                    double lon = objVal.get("lon").asDouble();
                    double valDouble = ((double[]) val)[0];
                    double delta = 0.05;
                    Assertions.assertEquals(lon, valDouble, delta);
                } else if (val instanceof Duration) {
                    Duration testDur = Duration.parse(reference.get(key).textValue());
                    Assertions.assertEquals(testDur, ((Duration)val));
                } else if (val instanceof Map) {
                    JsonNode objVal = objectMapper.readTree(reference.get(key).textValue());
                    for (Object k : ((Map)val).keySet()) {
                        Object v = ((Map)val).get(k).toString();
                        Assertions.assertEquals(objVal.get((String)k).toString(), v);
                    }
                } else {
                    Assertions.assertEquals(reference.get(key).asText(), val.toString());
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

            Assertions.assertEquals(7, row.size());

            for(String key : row.keySet()){
                Object val = row.get(key);
                Assertions.assertEquals(reference.get(key).textValue(), val);
            }
            i++;
        }
    }


    @Test
    public void testReadDataWithoutSchemaJson() throws Exception{
        File testDataDir = getTestDataDirectory();

        File file = new File("data/employee_data_string_missing_col.json");
        Table employeeTable = Table.fromSource(file, testDataDir);

        Iterator iter = employeeTable.iterator(true, false, false, false);

        String referenceContent =
                String.join("", Files.readAllLines(new File(testDataDir, "data/employee_data_string_missing_col.json").toPath()));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode referenceArr = objectMapper.readTree(referenceContent);


        Path tempDirPath = Files.createTempDirectory("datapackage-");
        employeeTable.writeCsv(new File(tempDirPath.toFile(), "table.csv"), null);

        int i = 0;

        while(iter.hasNext()){
            Map<String, Object> row = (Map<String, Object>)iter.next();
            JsonNode reference = referenceArr.get(i);

            Assertions.assertEquals(7, row.size());

            for(String key : row.keySet()){
                Object val = row.get(key);
                if (null == reference.get(key)) {
                    Assertions.assertNull(val);
                } else {
                    Assertions.assertEquals(reference.get(key).textValue(), val);
                }
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

            Assertions.assertEquals(3, row.length);
            Assertions.assertEquals(i, row[0]);

            String[] keys = (String[])row[1];
            for (int j = 0; j < keys.length; j++) {
                String key = keys[j];
                String val = ((String[])row[2])[j];
                Assertions.assertEquals(reference.get(key).textValue(), val);
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

        Table table = Table.fromSource(csvContent, schema, TableDataSource.getDefaultCsvFormat());

        Assertions.assertEquals(3, table.read().size());
        List<Object[]> actualData = table.read(true);
        for (int i = 0; i < actualData.size(); i++) {
            Assertions.assertTrue(actualData.get(i)[2] instanceof Number, "Expected Number " +
                    "for population figures, CR/LF problem");
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
