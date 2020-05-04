package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.schema.SchemaTest;
import io.frictionlessdata.tableschema.table_tests.TableCreationTest;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {


    public static File getTestDataDirectory()throws Exception {
        URL u = TestHelper.class.getResource("/fixtures/data/simple_data.csv");
        Path path = Paths.get(u.toURI());
        return path.getParent().getParent().toFile();
    }


    public static File getResourceFile(String fileName) throws URISyntaxException {
        try {
            // Create file-URL of source file:
            URL sourceFileUrl = SchemaTest.class.getResource(fileName);
            // normal case: resolve against resources path
            Path path = Paths.get(sourceFileUrl.toURI());
            return path.toFile();
        } catch (NullPointerException ex) {
            // special case for invalid path test
            return new File (fileName);
        }
    }
}
