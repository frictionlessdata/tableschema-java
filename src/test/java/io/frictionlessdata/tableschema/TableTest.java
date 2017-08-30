package io.frictionlessdata.tableschema;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
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
        
        Assert.assertEquals(3, table.read().size()); 
    }
    
    @Test
    public void testReadFromValidUrl() throws Exception{
        // get path of test CSV file
        URL url = new URL("https://raw.githubusercontent.com/frictionlessdata/tableschema-java/master/src/test/resources/fixtures/simple_data.csv");
        Table table = new Table(url);
        
        Assert.assertEquals(3, table.read().size());
    }
    
    @Test
    public void testInferTypesIntAndDates() throws Exception{
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/dates_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        JSONObject schema = table.inferSchema();
        JSONArray schemaFiles = schema.getJSONArray("fields");
        
        // The field names are the same as the name of the type we are expecting to be inferred.
        for(int i=0; i<schemaFiles.length(); i++){
            Assert.assertEquals(schemaFiles.getJSONObject(i).get("name"), schemaFiles.getJSONObject(i).get("type"));
        }
    }
    
    @Test
    public void testInferTypesIntBoolAndGeopoints() throws Exception{
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/int_bool_geopoint_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        JSONObject schema = table.inferSchema();
        JSONArray schemaFiles = schema.getJSONArray("fields");
        
        // The field names are the same as the name of the type we are expecting to be inferred.
        for(int i=0; i<schemaFiles.length(); i++){
            Assert.assertEquals(schemaFiles.getJSONObject(i).get("name"), schemaFiles.getJSONObject(i).get("type"));
        }
    }
    
    @Test
    public void testIterate() throws Exception{
        // get path of test CSV file
        String sourceFileAbsPath = TableTest.class.getResource("/fixtures/simple_data.csv").getPath();
        Table table = new Table(sourceFileAbsPath);
        
        String[] expectedResults = new String[]{"[1, foo]", "[2, bar]", "[3, baz]"};
        
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
