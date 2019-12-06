package io.frictionlessdata.tableschema.datasourceformats;

import com.google.common.collect.Iterators;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * 
 */
public abstract class AbstractDataSourceFormat implements DataSourceFormat {
    Object dataSource = null;
    File workDir;

    AbstractDataSourceFormat(){}


    public AbstractDataSourceFormat(URL dataSource){
        this.dataSource = dataSource;
    }

    public AbstractDataSourceFormat(File dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    public AbstractDataSourceFormat(String dataSource){
        this.dataSource = dataSource;
    }

    abstract CSVParser getCSVParser() throws Exception;

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


    String getFileContents(String path) throws IOException {
        String lines;
        if (workDir.getName().endsWith(".zip")) {
            //have to exchange the backslashes on Windows, as
            //zip paths are forward slashed.
            if (File.separator.equals("\\"))
                path = path.replaceAll("\\\\", "/");
            ZipFile zipFile = new ZipFile(workDir.getAbsolutePath());
            ZipEntry entry = zipFile.getEntry(path);
            InputStream stream = zipFile.getInputStream(entry);
            try (BufferedReader rdr = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                lines = rdr
                        .lines()
                        .collect(Collectors.joining("\n"));
            }

        } else {
            // The path value can either be a relative path or a full path.
            // If it's a relative path then build the full path by using the working directory.
            // Caution: here, we cannot simply use provided paths, we have to check
            // they are neither absolute path or relative parent paths (../)
            // see:
            //    - https://github.com/frictionlessdata/tableschema-java/issues/29
            //    - https://frictionlessdata.io/specs/data-resource/#url-or-path
            Path inPath = ((File)dataSource).toPath();
            Path resolvedPath = DataSourceFormat.toSecure(inPath, workDir.toPath());

            // Read the file.
            try (BufferedReader rdr = new BufferedReader(new FileReader(resolvedPath.toFile()))) {
                lines = rdr
                        .lines()
                        .collect(Collectors.joining("\n"));
            }
        }
        return lines;
    }

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is
     * null, then the file will be written as RFC 4180 compliant CSV
     * @param out the Writer to write to
     * @throws Exception thrown if write operation fails
     */
    @Override
    public void writeCsv(Writer out, CSVFormat format) {
        try {
            CSVFormat locFormat = (null != format)
                    ? format
                    : CSVFormat.RFC4180
                    .withHeader();
            CSVPrinter csvPrinter = new CSVPrinter(out, locFormat);

            String[] headers = getHeaders();
            if (headers != null) {
                csvPrinter.printRecord((Object[]) headers);
            }

            for (String[] record : data()) {
                csvPrinter.printRecord(record);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is
     * null, then the file will be written as RFC 4180 compliant CSV
     * @param outputFile the File to write to
     * @throws Exception thrown if write operation fails
     */
    @Override
    public void writeCsv(File outputFile, CSVFormat format) throws Exception {
        CSVFormat locFormat = (null != format)
                ? format
                : CSVFormat.RFC4180
                .withHeader();
        try (Writer out = new BufferedWriter(new FileWriter(outputFile))) {
            writeCsv(out, locFormat);
        }
    }

}
