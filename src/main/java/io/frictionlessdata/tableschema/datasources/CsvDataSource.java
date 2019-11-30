package io.frictionlessdata.tableschema.datasources;

import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 *
 */
public class CsvDataSource extends AbstractDataSource {

    private CSVFormat format = CSVFormat
            .RFC4180
            .withHeader();;

    public CsvDataSource(){};

    public CsvDataSource(InputStream inStream) throws Exception{
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(inputStreamReader);

        String content = br.lines().collect(Collectors.joining("\n"));
        this.dataSource = content;
        br.close();
        inputStreamReader.close();

        try {
            new JSONArray(content);
        } catch (JSONException ex) {
            try {
                new JSONObject(content);
            } catch (JSONException ex2) {
                return;
            }
        }

        throw new IllegalArgumentException("Input seems to be in JSON format");
    }

    public CsvDataSource(URL dataSource){
        super(dataSource);
    }
    
    public CsvDataSource(File dataSource, File workDir){
        super(dataSource, workDir);
    }
    
    public CsvDataSource(String dataSource){
        super(dataSource);
    }


    public CsvDataSource format(CSVFormat format) {
        this.format = format;
        return this;
    }

    public CSVFormat format() {
        return format;
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
        CSVFormat format = this.format;
        if (null == format) {
            format = CSVFormat
                    .RFC4180
                    .withHeader();
        }
        if(dataSource instanceof String){
            Reader sr = new StringReader((String)dataSource);
            return CSVParser.parse(sr, format);

        }else if(dataSource instanceof File){
            // The path value can either be a relative path or a full path.
            // If it's a relative path then build the full path by using the working directory.
            // Caution: here, we cannot simply use provided paths, we have to check
            // they are neither absolute path or relative parent paths (../)
            // see:
            //    - https://github.com/frictionlessdata/tableschema-java/issues/29
            //    - https://frictionlessdata.io/specs/data-resource/#url-or-path

            String lines = getFileContents(((File)dataSource).getPath());

            // Get the parser.
            //return CSVFormat.RFC4180.withHeader().parse(fr);
            return CSVParser.parse(lines, format);
            
        } else if(dataSource instanceof URL){
            return CSVParser.parse((URL)dataSource, StandardCharsets.UTF_8, format);
            //return CSVParser.parse((URL)dataSource, Charset.forName("UTF-8"), CSVFormat.RFC4180.withHeader());
            
        } else{
            throw new Exception("Data source is of invalid type.");
        }
    }

    @Override
    public void write(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile));
             CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.RFC4180)) {

            if (this.getHeaders() != null) {
                csvPrinter.printRecord(this.getHeaders());
            }

            Iterator<CSVRecord> recordIter = this.getCSVParser().iterator();
            while(recordIter.hasNext()){
                CSVRecord record = recordIter.next();
                csvPrinter.printRecord(record);
            }

            csvPrinter.flush();

        } catch (Exception e) {
            throw e;
        }
    }
}
