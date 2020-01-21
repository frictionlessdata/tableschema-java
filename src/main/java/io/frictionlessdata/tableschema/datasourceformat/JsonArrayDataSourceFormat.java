package io.frictionlessdata.tableschema.datasourceformat;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 *
 */
public class JsonArrayDataSourceFormat extends AbstractDataSourceFormat {

    public JsonArrayDataSourceFormat(String dataSource){
        super();
        dataSource = DataSourceFormat.trimBOM(dataSource);
        this.dataSource = new JSONArray(dataSource);
    }


    public JsonArrayDataSourceFormat (InputStream inStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inputStreamReader);

        String content = br.lines().collect(Collectors.joining("\n"));
        content = DataSourceFormat.trimBOM(content);
        br.close();
        inputStreamReader.close();
        this.dataSource = new JSONArray(content);
    }

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is
     * null, then the file will be written as RFC 4180 compliant CSV
     * @param out the Writer to write to
     * @param format the CSV format to use
     * @param sortedHeaders the header row names in the order in which data should be
     *                      exported
     */
    @Override
    public void writeCsv(Writer out, CSVFormat format, String[] sortedHeaders) {
        if (null == sortedHeaders) {
            writeCsv(out, format);
            return;
        }
        try {
            CSVFormat locFormat = (null != format)
                    ? format
                    : DataSourceFormat.getDefaultCsvFormat();

            locFormat = locFormat.withHeader(sortedHeaders);
            CSVPrinter csvPrinter = new CSVPrinter(out, locFormat);
            JSONArray data = (JSONArray)this.dataSource;
            for (Object record : data) {
                JSONObject obj = (JSONObject) record;
                writeData(obj, sortedHeaders, csvPrinter);
            }
            csvPrinter.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void writeData(JSONObject data, String[] sortedHeaders, CSVPrinter csvPrinter) {
        try {
            int recordLength = sortedHeaders.length;
            String[] sortedRec = new String[recordLength];
            for (int i = 0; i < recordLength; i++) {
                String key = sortedHeaders[i];
                if (data.has(key))
                    sortedRec[i] = data.get(key).toString();
            }
            csvPrinter.printRecord(sortedRec);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void write(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile))) {
            out.write(dataSource.toString());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean hasReliableHeaders() {
        return false;
    }

    /**
     * Retrieve the CSV Parser.
     * The parser works record wise. It is not possible to go back, once a
     * record has been parsed from the input stream. Because of this, CSVParser
     * needs to be recreated every time:
     * https://commons.apache.org/proper/commons-csv/apidocs/index.html?org/apache/commons/csv/CSVParser.html
     *
     * @return a CSVParser instance that works CSV data generated by converting the JSON-array data to CSV
     * @throws Exception thrown if the parser throws an exception
     */
    @Override
    CSVParser getCSVParser() throws Exception{
        String dataSourceString;
        if(dataSource instanceof JSONArray){
            dataSourceString = dataSource.toString();
        } else{
            throw new Exception("Data source is of invalid type.");
        }
        String dataCsv = CDL.toString(new JSONArray(dataSourceString));
        Reader sr = new StringReader(dataCsv);
        // Get the parser.
        return CSVParser.parse(sr, DataSourceFormat.getDefaultCsvFormat());
    }
}