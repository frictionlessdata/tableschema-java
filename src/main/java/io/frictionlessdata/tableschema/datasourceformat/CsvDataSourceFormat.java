package io.frictionlessdata.tableschema.datasourceformat;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 *
 */
public class CsvDataSourceFormat extends AbstractDataSourceFormat {

    private CSVFormat format = DataSourceFormat.getDefaultCsvFormat();

    CsvDataSourceFormat(InputStream inStream) throws Exception{
        try (InputStreamReader is = new InputStreamReader(inStream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(is)) {
            String content = br.lines().collect(Collectors.joining("\n"));
            this.dataSource = DataSourceFormat.trimBOM(content);

            // ensure that both parsing as JSON array and JSON object data fails. If one succeeds,
            // then the data is not CSV, but JSON -> throw exception
            try {
                new JSONArray((String)this.dataSource);
            } catch (JSONException ex) {
                try {
                    new JSONObject((String)this.dataSource);
                } catch (JSONException ex2) {
                    return;
                }
            }
            throw new IllegalArgumentException("Input seems to be in JSON format");
        }
    }

    CsvDataSourceFormat(URL dataSource){
        super(dataSource);
    }
    
    CsvDataSourceFormat(File dataSource, File workDir){
        super(dataSource, workDir);
    }

    CsvDataSourceFormat(String dataSource){
        super(dataSource);
    }


    public CsvDataSourceFormat setFormat(CSVFormat format) {
        this.format = format;
        return this;
    }

    public CSVFormat getFormat() {
        return format;
    }

    /**
     * Retrieve the CSV Parser.
     * The parser works record wise. It is not possible to go back, once a
     * record has been parsed from the input stream. Because of this, CSVParser
     * needs to be recreated every time:
     * https://commons.apache.org/proper/commons-csv/apidocs/index.html?org/apache/commons/csv/CSVParser.html
     * 
     * @return a CSVParser instance
     * @throws Exception if either the data has the wrong format or some I/O exception occurs
     */
    @Override
    CSVParser getCSVParser() throws Exception{
        CSVFormat format = (this.format != null)
                ? this.format
                : DataSourceFormat.getDefaultCsvFormat();

        if (dataSource instanceof String){
            return CSVParser.parse((String)dataSource, format);
        } else if(dataSource instanceof File){
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
            
        } else{
            throw new TableSchemaException("Data source is of invalid type.");
        }
    }

    @Override
    public void write(File outputFile) throws Exception {
        CSVFormat format = DataSourceFormat.getDefaultCsvFormat();
        super.writeCsv(outputFile, format, this.headers);
    }

    @Override
    public boolean hasReliableHeaders() {
        try {
            return this.getHeaders() != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
