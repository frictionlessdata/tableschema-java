package io.frictionlessdata.tableschema;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
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
            URL sourceFileUrl = TestHelper.class.getResource(fileName);
            // normal case: resolve against resources path
            Path path = Paths.get(sourceFileUrl.toURI());
            return path.toFile();
        } catch (NullPointerException ex) {
            // special case for invalid path test
            return new File (fileName);
        }
    }

    public static String getResourceFileContent(String fileName) throws URISyntaxException, IOException {
        File expectedFile = TestHelper.getResourceFile(fileName);
        return String.join("\n", Files.readAllLines(expectedFile.toPath()));
    }
}
