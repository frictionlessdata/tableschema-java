package io.frictionlessdata.tableschema;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * 
 */
public class TableTest {
    
    @Test
    public void testReadFromValidFilePath() throws Exception{
        // get path of test CSV file
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        Assert.assertEquals(4, table.read().size()); 
    }
    
    @Test
    public void testReadFromValidUrl() throws Exception{
        // get path of test CSV file
        URL url = new URL("https://github.com/frictionlessdata/tableschema-rb/raw/master/spec/fixtures/simple_data.csv");
        Table table = new Table(url);
        
        Assert.assertEquals(4, table.read().size()); 
    }
    
    @Test
    public void testIterate() throws Exception{
        // get path of test CSV file
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        String[] expectedResults = new String[]{"[id, title]", "[1, foo]", "[2, bar]", "[3, baz]"};
        
        Iterator<String[]> iter = table.iterator();
        int loopCounter = 0;
        while (iter.hasNext()) {
            String stringifiedRow = Arrays.toString(iter.next());
            Assert.assertEquals(expectedResults[loopCounter], stringifiedRow);
            loopCounter++;
        }
    }
    
    @Test
    public void testFetchHeaders() throws Exception{
        // get path of test CSV file
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        Assert.assertEquals("[id, title]", Arrays.toString(table.headers()));
    }
}
