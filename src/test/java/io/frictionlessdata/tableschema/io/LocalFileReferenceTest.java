package io.frictionlessdata.tableschema.io;

import io.frictionlessdata.tableschema.TestHelper;
import io.frictionlessdata.tableschema.schema.Schema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

class LocalFileReferenceTest {

    @Test
    @DisplayName("Loading a Schema from ZIP-FileReference")
    void csvParsingWithSchema() throws Exception{
        File baseFile = TestHelper.getTestDataDirectory();
        File zipFile = new File (baseFile, "schema/schema.zip");
        LocalFileReference lft = new LocalFileReference(zipFile, "schema/population_schema.json");
        InputStream is = lft.getInputStream();
        String content = null;
        try (InputStreamReader rdr = new InputStreamReader(is);
             BufferedReader bfr = new BufferedReader(rdr)) {
            content = bfr.lines().collect(Collectors.joining());
        }
        Assertions.assertNotNull(content);
        Schema testSchema = Schema.fromJson(content, true);
        File f = new File(TestHelper.getTestDataDirectory(), "schema/population_schema.json");
        Schema referenceSchema = Schema.fromJson(f, true);
        Assertions.assertEquals(referenceSchema, testSchema);
    }

    @Test
    @DisplayName("Loading a Schema from ZIP-FileReference one level deep")
    void csvParsingWithSchema2() throws Exception{
        String relPath = "population_schema.json";
        File baseFile = TestHelper.getTestDataDirectory();
        File zipFile = new File (baseFile, "schema/schema.zip");
        LocalFileReference lft = new LocalFileReference(zipFile, relPath);
        InputStream is = lft.getInputStream();
        String content = null;
        try (InputStreamReader rdr = new InputStreamReader(is);
             BufferedReader bfr = new BufferedReader(rdr)) {
            content = bfr.lines().collect(Collectors.joining());
        }
        Assertions.assertNotNull(content);
        Schema testSchema = Schema.fromJson(content, true);
        File f = new File(TestHelper.getTestDataDirectory(), "schema/population_schema.json");
        Schema referenceSchema = Schema.fromJson(f, true);
        Assertions.assertEquals(referenceSchema, testSchema);
        Assertions.assertEquals("population_schema.json", lft.getFileName());
        Assertions.assertEquals(zipFile.getAbsolutePath()+File.separator+relPath, lft.getLocator());
    }


    @Test
    @DisplayName("Loading a Schema from URL")
    void fileReferenceParsingFromUrl() throws Exception{
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/" +
                "master/src/test/resources/fixtures/schema/simple_schema.json");

        URLFileReference lft = new URLFileReference(url);
        InputStream is = lft.getInputStream();
        String content;
        try (InputStreamReader rdr = new InputStreamReader(is);
             BufferedReader bfr = new BufferedReader(rdr)) {
            content = bfr.lines().collect(Collectors.joining());
        }
        Assertions.assertNotNull(content);
        Schema testSchema = Schema.fromJson(url, true);
        File f = new File(TestHelper.getTestDataDirectory(), "schema/simple_schema.json");
        Schema referenceSchema = Schema.fromJson(f, true);
        Assertions.assertEquals(referenceSchema, testSchema);
        Assertions.assertEquals("simple_schema.json", lft.getFileName());
        Assertions.assertEquals(url.toExternalForm(), lft.getLocator());
    }
}
