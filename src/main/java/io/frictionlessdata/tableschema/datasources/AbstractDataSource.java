package io.frictionlessdata.tableschema.datasources;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;

import java.io.*;
import java.net.URL;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * 
 */
public abstract class AbstractDataSource implements DataSource {
    Object dataSource = null;
    File workDir;

    public AbstractDataSource(InputStream inStream) throws IOException{
        try (InputStreamReader ir = new InputStreamReader(inStream)) {
            try (BufferedReader rdr = new BufferedReader(ir)) {
                String dSource = rdr.lines().collect(Collectors.joining("\n"));
                this.dataSource = new JSONArray(dSource);
            }
        }
    }

    public AbstractDataSource(URL dataSource){
        this.dataSource = dataSource;
    }

    public AbstractDataSource(File dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    public AbstractDataSource(String dataSource){
        this.dataSource = dataSource;
    }

    @Override
    abstract public Iterator<String[]> iterator() throws Exception;
    
    @Override
    abstract public String[] getHeaders() throws Exception;
    
    @Override
    abstract public List<String[]> data() throws Exception;
    
    @Override
    abstract public void write(File outputFile) throws Exception;

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
            Path resolvedPath = DataSource.toSecure(inPath, workDir.toPath());

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
     * @param outputFile the File to write to
     * @throws Exception thrown if write operation fails
     */
    @Override
    public void writeCsv(File outputFile, CSVFormat format) throws Exception {
        CSVFormat locFormat = (null != format)
                ? format
                : CSVFormat.RFC4180
                .withHeader();
        try (Writer out = new BufferedWriter(new FileWriter(outputFile));
             CSVPrinter csvPrinter = new CSVPrinter(out, locFormat)) {

            String[] headers = getHeaders();
            if (headers != null) {
                csvPrinter.printRecord((Object[]) headers);
            }

            for (String[] record : data()) {
                csvPrinter.printRecord(record);
            }

            csvPrinter.flush();

        } catch (Exception e) {
            throw e;
        }
    }
}
