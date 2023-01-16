package io.frictionlessdata.tableschema.tabledatasource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements a {@link TableDataSource} based on CSV-Data in a string.
 *
 * To define CSV properties like record delimiters or whether the data has a header row,
 * the class uses the Apache Commons CSV {@link CSVFormat} class.
 * Actual parsing is done via the {@link CSVParser} from the same project.
 *
 * The default CSV format for this class is based on RFC4180 (https://www.rfc-editor.org/rfc/rfc4180)
 * with a header row, ignoring whitespace around column values and "\n" as a record separator.
 */
public class CsvTableDataSource extends AbstractTableDataSource<String> {

    private CSVFormat format = TableDataSource.getDefaultCsvFormat();

    /**
     * Construct a TableDataSource from a CSV-containig String.
     *
     * @param dataSource the String to read data from
     */
    CsvTableDataSource(String dataSource){
        this.dataSource = dataSource;
    }

    public void setFormat(CSVFormat format) {
        this.format = format;
    }

    public CSVFormat getFormat() {
        return (this.format != null)
                ? this.format
                : TableDataSource.getDefaultCsvFormat();
    }


    @Override
    public Iterator<String[]> iterator(){
        CSVParser parser;
        try {
            parser = getCSVParser();
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        Iterator<CSVRecord> iterCSVRecords = parser.iterator();

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
    public String[] getHeaders(){
        if (null == headers) {
            // Get a copy of the header map that iterates in column order.
            // The map keys are column names. The map values are 0-based indices.
            Map<String, Integer> headerMap = null;
            try {
                headerMap = getCSVParser().getHeaderMap();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            if (null == headerMap) {
                return null;
            }
             headers = headerMap.keySet().toArray(new String[0]);
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
     * @throws IOException if either the data has the wrong format or some I/O exception occurs
     */
    private CSVParser getCSVParser() throws IOException {
        CSVFormat format = getFormat();

        if (null != dataSource){
            return CSVParser.parse(dataSource, format);
        }
        return null;
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
