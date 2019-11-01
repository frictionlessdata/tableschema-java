package io.frictionlessdata.tableschema;

import com.sun.javafx.PlatformUtil;
import io.frictionlessdata.tableschema.datasources.DataSource;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataSourceTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testUnsafePath1() throws Exception {
       URL u = DataSourceTest.class.getResource("/fixtures/dates_data.csv");
       Path path = Paths.get(u.toURI());
       Path testPath = path.getParent();
       String maliciousPathName = "/etc/passwd";
       if (PlatformUtil.isWindows()){
           maliciousPathName = "C:/Windows/system.ini";
       }
       Path maliciousPath = new File(maliciousPathName).toPath();
       exception.expect(IllegalArgumentException.class);
       DataSource.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testUnsafePath2() throws Exception {
        URL u = DataSourceTest.class.getResource("/fixtures/dates_data.csv");
        Path path = Paths.get(u.toURI());
        Path testPath = path.getParent();
        String maliciousPathName = "/etc/";
        if (PlatformUtil.isWindows()){
            maliciousPathName = "C:/Windows/";
        }
        Path maliciousPath = new File(maliciousPathName).toPath();
        exception.expect(IllegalArgumentException.class);
        DataSource.toSecure(maliciousPath, testPath);
    }

    @Test
    public void testSafePath() throws Exception {
        URL u = DataSourceTest.class.getResource("/fixtures/dates_data.csv");
        Path path = Paths.get(u.toURI());
        Path testPath = path.getParent().getParent();
        String maliciousPathName = "fixtures/dates_data.csv";
        if (PlatformUtil.isWindows()){
            maliciousPathName = "fixtures/dates_data.csv";
        }
        Path maliciousPath = new File(maliciousPathName).toPath();
        DataSource.toSecure(maliciousPath, testPath);
    }
}

