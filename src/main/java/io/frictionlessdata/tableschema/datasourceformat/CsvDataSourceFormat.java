package io.frictionlessdata.tableschema.datasourceformat;

import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class CsvDataSourceFormat extends AbstractDataSourceFormat {

    private CSVFormat format = DataSourceFormat.getDefaultCsvFormat();

    /**
     * Constructor from a Stream. In contrast to lazy-loading File- or URL-based constructors, this one
     * reads all the data at construction time.
     * @param inStream the stream to read from
     * @throws Exception if an IOException occurs
     */
    CsvDataSourceFormat(InputStream inStream) throws Exception{
        try (InputStreamReader is = new InputStreamReader(inStream, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(is)) {
            String content = br.lines().collect(Collectors.joining("\n"));
            this.dataSource = DataSourceFormat.trimBOM(content);

            // ensure that both parsing as json fails. If it succeeds,
            // then the data is not CSV, but JSON -> throw exception
            try {
                JsonUtil.getInstance().readValue((String)this.dataSource);
            } catch (Exception ex) {
                return;
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
        return (this.format != null)
                ? this.format
                : DataSourceFormat.getDefaultCsvFormat();
    }


    @Override
    public Iterator<String[]> iterator() throws Exception{
        Iterator<CSVRecord> iterCSVRecords = this.getCSVParser().iterator();

        return Iterators.transform(iterCSVRecords, (CSVRecord input) -> {
            Iterator<String> iterCols = input.iterator();

            List<String> cols = new ArrayList<>();
            while(iterCols.hasNext()){
                cols.add(iterCols.next());
            }

            return cols.toArray(new String[0]);
        });
    }

    @Override
    public String[] getHeaders() throws Exception{
        if (null == headers) {
            // Get a copy of the header map that iterates in column order.
            // The map keys are column names. The map values are 0-based indices.
            Map<String, Integer> headerMap = this.getCSVParser().getHeaderMap();

            // Generate list of keys
            List<String> headerVals = new ArrayList<>();

            headerMap.entrySet().forEach((pair) -> {
                headerVals.add(pair.getKey());
            });

            headers = headerVals.toArray(new String[0]);
        }
        return headers;
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
    private CSVParser getCSVParser() throws Exception{
        CSVFormat format = getFormat();

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
    public boolean hasReliableHeaders() {
        try {
            return this.getHeaders() != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
