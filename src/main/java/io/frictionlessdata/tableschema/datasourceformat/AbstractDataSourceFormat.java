package io.frictionlessdata.tableschema.datasourceformat;

import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * 
 */
public abstract class AbstractDataSourceFormat implements DataSourceFormat {
    String[] headers;
    Object dataSource = null;
    private File workDir;

    AbstractDataSourceFormat(){}

    AbstractDataSourceFormat(URL dataSource){
        this.dataSource = dataSource;
    }

    AbstractDataSourceFormat(File dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    AbstractDataSourceFormat(String dataSource){
        this.dataSource = dataSource;
    }

    abstract CSVParser getCSVParser() throws Exception;

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
    public List<String[]> data() throws Exception{
        List<String[]> data = new ArrayList<>();
        iterator().forEachRemaining(data::add);
        return data;
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

    String getFileContents(String path) throws IOException {
        return DataSourceFormat.getFileContents(path, workDir);
    }

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is
     * null, then the file will be written as RFC 4180 compliant CSV
     * @param outputFile the File to write to
     * @param format the CSV format to use
     * @param sortedHeaders the header row names in the order in which data should be
     *                      exported
     */
    @Override
    public void writeCsv(File outputFile, CSVFormat format, String[] sortedHeaders) throws Exception {
        CSVFormat locFormat = (null != format)
                ? format
                : DataSourceFormat.getDefaultCsvFormat();
        try (Writer out = new BufferedWriter(new FileWriter(outputFile))) {
            writeCsv(out, locFormat, sortedHeaders);
        }
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
        try {
            if (null == sortedHeaders) {
                writeCsv(out, format, getHeaders());
                return;
            }
            CSVFormat locFormat = (null != format)
                    ? format
                    : DataSourceFormat.getDefaultCsvFormat();

            locFormat = locFormat.withHeader(sortedHeaders);
            CSVPrinter csvPrinter = new CSVPrinter(out, locFormat);

            String[] headers = getHeaders();
            Map<Integer, Integer> mapping
                    = TableSchemaUtil.createSchemaHeaderMapping(headers, sortedHeaders);
            writeData(data(), mapping, csvPrinter);
            csvPrinter.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static void writeData(List<String[]> data, Map<Integer, Integer> mapping, CSVPrinter csvPrinter) {
        try {
            for (String[] record : data) {
                String[] sortedRec = new String[record.length];
                for (int i = 0; i < record.length; i++) {
                    sortedRec[mapping.get(i)] = record[i];
                }
                csvPrinter.printRecord(sortedRec);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
