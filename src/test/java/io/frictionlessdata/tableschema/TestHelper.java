package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.table_tests.TableCreationTest;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {


    public static File getTestDataDirectory()throws Exception {
        URL u = TableCreationTest.class.getResource("/fixtures/simple_data.csv");
        Path path = Paths.get(u.toURI());
        return path.getParent().toFile();
    }
}
