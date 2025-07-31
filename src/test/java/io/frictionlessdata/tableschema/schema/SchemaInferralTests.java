package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.beans.EmployeeBeanWithAnnotation;
import io.frictionlessdata.tableschema.beans.ExplicitNamingBean;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.frictionlessdata.tableschema.TestHelper.getResourceFile;
import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaInferralTests {

    @Test
    @DisplayName("Infer a Schema and test MapValueComparator")
    void inferASchema() throws Exception{
        File basePath = getResourceFile("/fixtures/data/");
        File source = getResourceFile("employee_data.csv");
        Table table = Table.fromSource(source, basePath);

        Schema schema = table.inferSchema();

        ObjectMapper objectMapper = new ObjectMapper();
        Object jsonObject = objectMapper.readValue(schema.asJson(), Object.class);
        String expectedString = TestHelper.getResourceFileContent(
                "/fixtures/schema/employee_schema.json");
        assertEquals(objectMapper.readValue(expectedString, Object.class), jsonObject);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Infer a Bean Schema")
    void inferExplicitNamingBeanSchema() throws Exception{
        Schema schema = BeanSchema.infer(ExplicitNamingBean.class);
        String expectedSchemaString = TestHelper.getResourceFileContent(
                "/fixtures/beans/explicitnamingbean-schema.json");
        Schema expectedSchema = Schema.fromJson(expectedSchemaString, true);

        Assertions.assertEquals(expectedSchema, schema);
        // validate equals() method implementation on Schema and BeanSchema
        // don't simplify this to assertEquals()
        Assertions.assertTrue(schema.equals(expectedSchema));
        Assertions.assertTrue(expectedSchema.equals(schema));
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Infer a Bean Schema, fix for https://github.com/frictionlessdata/tableschema-java/issues/72")
    void inferSchemaWithEmptyColumns() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        File basePath = getResourceFile("/fixtures/data/");

        // first, a CSV with empty cols
        File source = getResourceFile("AW_229_000001_000002.csv");
        Table table = Table.fromSource(source, basePath);

        Schema schema = table.inferSchema();

        Object jsonObject = objectMapper.readValue(schema.asJson(), Object.class);
        String expectedString = TestHelper.getResourceFileContent(
                "/fixtures/schema/issue-72.json");
        assertEquals(objectMapper.readValue(expectedString, Object.class), jsonObject);


        // then, a CSV with quoted empty cols
        File source2 = getResourceFile("AW_229_000001_000002_1Row.csv");
        Table table2 = Table.fromSource(source2, basePath);

        Schema schema2 = table2.inferSchema();

        jsonObject = objectMapper.readValue(schema2.asJson(), Object.class);
        assertEquals(objectMapper.readValue(expectedString, Object.class), jsonObject);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test inferal of Schema from EmployeeBean with Annotation")
    void testBeanDeserialization2() throws Exception {
        File testDataDir = getTestDataDirectory();
        File inFile = new File(testDataDir, "schema/employee_full_schema_no_primary_secondary_keys.json");
        Schema reference = Schema.fromJson(inFile, true);
        Schema testSchema = BeanSchema.infer(EmployeeBeanWithAnnotation.class);
        assertEquals(reference, testSchema);
    }

    @Test
    @DisplayName("Test infer from String data")
    void testInferFromStringData() throws Exception {
        String csvData = "id,name,age\n1,John,30\n2,Jane,25\n3,Bob,35";
        Schema schema = Schema.infer(csvData, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertEquals(3, schema.getFields().size());
        Assertions.assertEquals("id", schema.getField("id").getName());
        Assertions.assertEquals("name", schema.getField("name").getName());
        Assertions.assertEquals("age", schema.getField("age").getName());
    }

    @Test
    @DisplayName("Test infer from String array")
    void testInferFromStringArray() throws Exception {
        String csvData = "id,name,age\n1,John,30\n2,Jane,25\n3,Bob,35";
        String csvData2 = "id,name,age\n3,Jack,32\n4,Anne,26";
        String[] array = new String[]{csvData, csvData2};
        Schema schema = Schema.infer(array, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertEquals(3, schema.getFields().size());
        Assertions.assertEquals("id", schema.getField("id").getName());
        Assertions.assertEquals("name", schema.getField("name").getName());
        Assertions.assertEquals("age", schema.getField("age").getName());
    }

    @Test
    @DisplayName("Test infer from String List")
    void testInferFromStringList() throws Exception {
        String csvData = "id,name,age\n1,John,30\n2,Jane,25\n3,Bob,35";
        String csvData2 = "id,name,age\n3,Jack,32\n4,Anne,26";
        List<String> strList = Arrays.asList(csvData, csvData2);
        Schema schema = Schema.infer(strList, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertEquals(3, schema.getFields().size());
        Assertions.assertEquals("id", schema.getField("id").getName());
        Assertions.assertEquals("name", schema.getField("name").getName());
        Assertions.assertEquals("age", schema.getField("age").getName());
    }

    @Test
    @DisplayName("Test infer from ArrayNode data")
    void testInferFromArrayNodeData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = "[{\"id\":1,\"name\":\"John\",\"age\":30},{\"id\":2,\"name\":\"Jane\",\"age\":25}]";
        ArrayNode arrayNode = (ArrayNode) mapper.readTree(jsonData);

        Schema schema = Schema.infer(arrayNode, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertEquals(3, schema.getFields().size());
        Assertions.assertTrue(schema.hasField("id"));
        Assertions.assertTrue(schema.hasField("name"));
        Assertions.assertTrue(schema.hasField("age"));
    }

    @Test
    @DisplayName("Test infer from List containing File objects")
    void testInferFromListWithFiles() throws Exception {
        File testFile = getResourceFile("/testsuite-data/files/csv/1mb.csv");
        File testFile2 = getResourceFile("/testsuite-data/files/csv/10mb.csv");
        List<File> fileList = Arrays.asList(testFile, testFile2);

        Schema schema = Schema.infer(fileList, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from List containing URL objects")
    void testInferFromListWithUrls() throws Exception {
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/data/simple_data.csv");
        List<URL> urlList = Arrays.asList(url);

        Schema schema = Schema.infer(urlList, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from List containing file path strings")
    void testInferFromListWithFilePaths() throws Exception {
        File testFile = getResourceFile("/fixtures/data/employee_data.csv");
        List<String> pathList = Arrays.asList(testFile.getAbsolutePath());

        Schema schema = Schema.infer(pathList, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from List containing URL strings")
    void testInferFromListWithUrlStrings() throws Exception {
        List<String> urlList = Arrays.asList(
                "https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                        "master/src/test/resources/fixtures/data/simple_data.csv"
        );

        Schema schema = Schema.infer(urlList, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from File array")
    void testInferFromFileArray() throws Exception {
        File testFile = getResourceFile("/fixtures/data/employee_data.csv");
        File[] fileArray = new File[]{testFile};

        Schema schema = Schema.infer(fileArray, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from URL array")
    void testInferFromUrlArray() throws Exception {
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/data/simple_data.csv");
        URL[] urlArray = new URL[]{url};

        Schema schema = Schema.infer(urlArray, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from String array containing file paths")
    void testInferFromStringArrayWithPaths() throws Exception {
        File testFile = getResourceFile("/fixtures/data/employee_data.csv");
        String[] pathArray = new String[]{testFile.getAbsolutePath()};

        Schema schema = Schema.infer(pathArray, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from mixed List with multiple sources yields identical schemas")
    void testInferFromMixedListWithIdenticalSchemas() throws Exception {
        String csvData = "id,name\n1,John\n2,Jane";
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), csvData.getBytes(StandardCharsets.UTF_8));

        List<Object> mixedList = Arrays.asList(csvData, tempFile);

        Schema schema = Schema.infer(mixedList, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertEquals(2, schema.getFields().size());
        Assertions.assertTrue(schema.hasField("id"));
        Assertions.assertTrue(schema.hasField("name"));
    }

    @Test
    @DisplayName("Test infer throws exception for different schemas from multiple sources")
    void testInferThrowsForDifferentSchemas() throws Exception {
        String csvData1 = "id,name\n1,John\n2,Jane";
        String csvData2 = "id,name,age\n1,John,30\n2,Jane,25";

        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), csvData2.getBytes(StandardCharsets.UTF_8));

        List<Object> mixedList = Arrays.asList(csvData1, tempFile);

        Assertions.assertThrows(IllegalStateException.class, () ->
                Schema.infer(mixedList, StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Test infer with null data throws exception")
    void testInferWithNullData() {
        Assertions.assertThrows(IllegalStateException.class, () ->
                Schema.infer(null, StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Test infer with unsupported data type throws exception")
    void testInferWithUnsupportedDataType() {
        Integer unsupportedData = 123;

        Assertions.assertThrows(IllegalStateException.class, () ->
                Schema.infer(unsupportedData, StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Test infer with List containing invalid objects throws exception")
    void testInferFromListWithInvalidObjects() {
        List<Object> invalidList = Arrays.asList(123, new Object());

        Assertions.assertThrows(TypeInferringException.class, () ->
                Schema.infer(invalidList, StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Test infer with different charsets")
    void testInferWithDifferentCharsets() throws Exception {
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/data/simple_data.csv");
        List<URL> urlList = Arrays.asList(url);

        Schema schemaUtf8 = Schema.infer(urlList, StandardCharsets.UTF_8);
        Schema schemaUtf16 = Schema.infer(urlList, StandardCharsets.UTF_16);

        Assertions.assertNotNull(schemaUtf8);
        Assertions.assertNotNull(schemaUtf16);
        // Note: schemas might differ if the source isn't compatible with UTF-16
    }

    @Test
    @DisplayName("Test infer with empty List throws exception")
    void testInferWithEmptyList() {
        List<String> emptyList = new ArrayList<>();

        Assertions.assertThrows(IllegalStateException.class, () ->
                Schema.infer(emptyList, StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Test infer with List containing null elements")
    void testInferWithListContainingNulls() throws Exception {
        File testFile = getResourceFile("/fixtures/data/employee_data.csv");
        List<Object> listWithNulls = Arrays.asList(testFile, null);

        Schema schema = Schema.infer(listWithNulls, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.getFields().size() > 0);
        Assertions.assertTrue(schema.isValid());
    }

    @Test
    @DisplayName("Test infer from CSV data with mixed types in name column")
    void testInferFromDataWithMixedTypes() throws Exception {
        Schema schema = Schema.infer(DATA, StandardCharsets.UTF_8);

        Assertions.assertNotNull(schema);
        Assertions.assertEquals(3, schema.getFields().size());

        // Verify field names
        Assertions.assertEquals("id", schema.getField("id").getName());
        Assertions.assertEquals("name", schema.getField("name").getName());
        Assertions.assertEquals("age", schema.getField("age").getName());

        // Verify field types
        Assertions.assertEquals("integer", schema.getField("id").getType());
        Assertions.assertEquals("string", schema.getField("name").getType());
        Assertions.assertEquals("integer", schema.getField("age").getType());

        // Additional validation
        Assertions.assertTrue(schema.isValid());
    }

    private String DATA =
            "id,name,age\n" +
            "1,John Smith,25\n" +
            "2,Emma Johnson,32\n" +
            "3,Michael Brown,28\n" +
            "4,Sarah Williams,45\n" +
            "5,David Jones,37\n" +
            "6,1456,29\n" +
            "7,Lisa Davis,41\n" +
            "8,James Miller,33\n" +
            "9,Jennifer Wilson,27\n" +
            "10,Robert Taylor,52\n" +
            "11,Maria Garcia,38\n" +
            "12,William Anderson,46\n" +
            "13,Elizabeth Martinez,31\n" +
            "14,Christopher Lee,39\n" +
            "15,1823,26\n" +
            "16,Patricia Thompson,44\n" +
            "17,Daniel White,35\n" +
            "18,Barbara Harris,48\n" +
            "19,Joseph Clark,30\n" +
            "20,Nancy Lewis,42";
}
