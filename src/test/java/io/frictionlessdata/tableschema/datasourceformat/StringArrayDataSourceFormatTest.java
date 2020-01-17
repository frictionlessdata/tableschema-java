package io.frictionlessdata.tableschema.datasourceformat;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.schema.Schema;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.frictionlessdata.tableschema.TestHelper.getTestDataDirectory;

class StringArrayDataSourceFormatTest {
    private static List<String[]> populationArr;

    private final static String populationCsv =
            "london,2017,8780000\n" +
            "paris,2017,2240000\n" +
            "rome,2017,2860000";

    private final static String populationjson = "[\n" +
            "  {\n" +
            "    \"city\": \"london\",\n" +
            "    \"year\": \"2017\",\n" +
            "    \"population\": 8780000\n" +
            "  },\n" +
            "  {\n" +
            "    \"city\": \"paris\",\n" +
            "    \"year\": \"2017\",\n" +
            "    \"population\": 2240000\n" +
            "  },\n" +
            "  {\n" +
            "    \"city\": \"rome\",\n" +
            "    \"year\": \"2017\",\n" +
            "    \"population\": 2860000\n" +
            "  }\n" +
            "]";

    private final static String[] populationHeaders = new String[]{
            "city", "year", "population"
    };


    @BeforeAll
    static void setUp() {
        String[] lines = populationCsv.split("\n");
        populationArr = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String[] cols = lines[i].split(",");
            populationArr.add(cols);
        }
    }

    @Test
    @DisplayName("Test StringArrayDataSourceFormat creation and data reading")
    void testReadStringArrayDataSourceFormat() throws Exception{
        DataSourceFormat ds = new StringArrayDataSourceFormat(populationArr, populationHeaders);
        List<String[]> data = ds.data();
        Assertions.assertEquals(populationArr, data);
    }

    @Test
    @DisplayName("Test get content as JSON")
    void testToJson() throws Exception{
        File schemaFile = new File(getTestDataDirectory(), "schema/population_schema.json");
        Schema schema = Schema.fromJson (schemaFile, true);
        DataSourceFormat ds = new StringArrayDataSourceFormat(populationArr, populationHeaders);
        String s = ds.asJson(schema);
        Assertions.assertEquals(populationjson, s);
    }

    @Test
    @DisplayName("Test writing data as CSV")
    void writeCsvToFile() throws Exception{
        String content = null;
        File tempFile = Files.createTempFile("tableschema-", ".csv").toFile();

        DataSourceFormat ds = new StringArrayDataSourceFormat(populationArr, populationHeaders);
        ds.write(tempFile);

        try (FileReader fr = new FileReader(tempFile)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }
        String testStr = String.join(",", populationHeaders) +"\n"+populationCsv;
        // evade the CRLF mess by nuking all CR chars
        Assertions.assertEquals(testStr.replaceAll("\\r", ""), content.replaceAll("\\r", ""));
    }

    @Test
    @DisplayName("Test method is unimplemented")
    void testGetCSVParserIsUnimplemented() throws Exception{
        StringArrayDataSourceFormat ds = new StringArrayDataSourceFormat(populationArr, populationHeaders);
        Assertions.assertThrows(TableSchemaException.class, ds::getCSVParser);
    }

}
