package io.frictionlessdata.tableschema.datasources;

import com.google.common.collect.Iterators;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class JsonArrayDataSource extends AbstractDataSource {

    public JsonArrayDataSource(InputStream inStream, File workDir) throws IOException{
        super(inStream, workDir);
    }

    public JsonArrayDataSource(URL dataSource, File workDir){
        super(dataSource, workDir);
    }

    public JsonArrayDataSource(File dataSource, File workDir){
        super(dataSource, workDir);
    }

    JsonArrayDataSource(JSONArray dataSource, File workDir){
        super(dataSource.toString(), workDir);
    }


    @Override
    public Iterator<String[]> iterator() throws Exception{
        Iterator<CSVRecord> iterCSVRecords = this.getCSVParser().iterator();

        Iterator<String[]> iterStringArrays = Iterators.transform(iterCSVRecords, (CSVRecord input) -> {
            Iterator<String> iterCols = input.iterator();

            List<String> cols = new ArrayList();
            while(iterCols.hasNext()){
                cols.add(iterCols.next());
            }

            String[] output = cols.toArray(new String[0]);

            return output;
        });

        return iterStringArrays;
    }

    @Override
    public List<String[]> data() throws Exception{
        // This is pretty much what happens when we call this.parser.getRecords()...
        Iterator<CSVRecord> iter = this.getCSVParser().iterator();
        List<String[]> data = new ArrayList();

        while(iter.hasNext()){
            CSVRecord record = iter.next();
            Iterator<String> colIter = record.iterator();

            //...except that we want list of String[] rather than list of CSVRecord.
            List<String> cols = new ArrayList();
            while(colIter.hasNext()){
                cols.add(colIter.next());
            }

            data.add(cols.toArray(new String[0]));
        }

        return data;
    }

    @Override
    public void write(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile));
             CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.RFC4180)) {

            if (this.getHeaders() != null) {
                csvPrinter.printRecord((Object[]) this.getHeaders());
            }

            for (CSVRecord record : this.getCSVParser()) {
                csvPrinter.printRecord(record);
            }

            csvPrinter.flush();

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String[] getHeaders() throws Exception{
        try{
            // Get a copy of the header map that iterates in column order.
            // The map keys are column names. The map values are 0-based indices.
            Map<String, Integer> headerMap = this.getCSVParser().getHeaderMap();

            // Generate list of keys
            List<String> headerVals = new ArrayList();

            headerMap.entrySet().forEach((pair) -> {
                headerVals.add((String)pair.getKey());
            });

            // Return string array of keys.
            return headerVals.toArray(new String[0]);

        }catch(Exception e){
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
    private CSVParser getCSVParser() throws Exception{
        String dataSourceString;
        if (dataSource instanceof String){
            dataSourceString = (String)dataSource;

        } else if(dataSource instanceof File){
            // The path value can either be a relative path or a full path.
            // If it's a relative path then build the full path by using the working directory.
            // Caution: here, we cannot simply use provided paths, we have to check
            // they are neither absolute path or relative parent paths (../)
            // see:
            //    - https://github.com/frictionlessdata/tableschema-java/issues/29
            //    - https://frictionlessdata.io/specs/data-resource/#url-or-path
            Path inPath = ((File)dataSource).toPath();
            Path resolvedPath = DataSource.toSecure(inPath, workDir.toPath());

           try (BufferedReader fr = new BufferedReader(new FileReader(resolvedPath.toFile()))) {
               dataSourceString = fr.lines().collect(Collectors.joining("\n"));
           }

        } else if(dataSource instanceof URL){
            return CSVParser.parse((URL)dataSource, Charset.forName("UTF-8"), CSVFormat.RFC4180.withHeader());

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
