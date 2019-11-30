package io.frictionlessdata.tableschema.datasources;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.json.CDL;
import org.json.JSONArray;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 *
 */
public class JsonArrayDataSource extends AbstractDataSource {

    JsonArrayDataSource(JSONArray dataSource){
        super(dataSource.toString());
    }

    public JsonArrayDataSource(InputStream inStream) throws IOException {
       //this(new JSONArray(inStream));
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(inputStreamReader);

        String content = br.lines().collect(Collectors.joining("\n"));
        br.close();
        inputStreamReader.close();
        JSONArray arr = new JSONArray(content);
        this.dataSource = arr;
    }


    @Override
    public void write(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile))) {
            out.write((String)dataSource);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Retrieve the CSV Parser.
     * The parser works record wise. It is not possible to go back, once a
     * record has been parsed from the input stream. Because of this, CSVParser
     * needs to be recreated every time:
     * https://commons.apache.org/proper/commons-csv/apidocs/index.html?org/apache/commons/csv/CSVParser.html
     *
     * @return
     * @throws Exception
     */
    @Override
    CSVParser getCSVParser() throws Exception{
        String dataSourceString;
        if (dataSource instanceof String){
            dataSourceString = (String)dataSource;

        } else if(dataSource instanceof JSONArray){
            dataSourceString = dataSource.toString();

        } else{
            throw new Exception("Data source is of invalid type.");
        }
        String dataCsv = CDL.toString(new JSONArray(dataSourceString));
        Reader sr = new StringReader(dataCsv);
        // Get the parser.
        return CSVParser.parse(sr, CSVFormat.RFC4180.withHeader());
    }
}
