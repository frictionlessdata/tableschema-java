package io.frictionlessdata.tableschema.tabledatasource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class StringArrayTableDataSourceTest {
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
    @DisplayName("Test StringArrayTableDataSource creation and data reading")
    void testReadStringArrayDataSourceFormat() throws Exception{
        TableDataSource ds = new StringArrayTableDataSource(populationArr, populationHeaders);
        List<String[]> data = ds.getDataAsStringArray();
        Assertions.assertEquals(populationArr, data);
    }
}
