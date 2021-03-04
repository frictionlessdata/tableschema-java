package io.frictionlessdata.tableschema.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.TestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static io.frictionlessdata.tableschema.TestHelper.getResourceFile;
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
        Object jsonObject = objectMapper.readValue(schema.getJson(), Object.class);
        String expectedString = TestHelper.getResourceFileContent(
                "/fixtures/schema/employee_schema.json");
        assertEquals(objectMapper.readValue(expectedString, Object.class), jsonObject);
    }
}
