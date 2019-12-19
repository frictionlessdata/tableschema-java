/*
 *
 */
package io.frictionlessdata.tableschema.datasourceformats;

import org.apache.commons.csv.CSVFormat;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface for a source of tabular data.
 */
public interface DataSourceFormat {
    Iterator<String[]> iterator() throws Exception;
    String[] getHeaders() throws Exception;
    void setHeaders(String[] newHeaders);
    List<String[]> data() throws Exception;

    /**
     * Write to native format
     * @param outputFile the File to write to
     * @throws Exception thrown if write operation fails
     */
    void write(File outputFile) throws Exception;

    /**
     * Write as CSV, the `format` parameter decides on the CSV options
     * @param out the Writer to write to
     * @throws Exception thrown if write operation fails
     */
    void writeCsv(Writer out, CSVFormat format);

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options
     * @param outputFile the File to write to
     * @throws Exception thrown if write operation fails
     */
    void writeCsv(File outputFile, CSVFormat format) throws Exception;

    /**
     * Factory method to instantiate either a JsonArrayDataSource or a
     * CsvDataSource based on input format
     * @return DataSource created from input String
     */
    static DataSourceFormat createDataSourceFormat(String input) {
        try {
            JSONArray arr = new JSONArray(input);
            return new JsonArrayDataSourceFormat(arr);
        } catch (JSONException ex) {
            // JSON parsing failed, treat it as a CSV
            return new CsvDataSourceFormat(input);
        }
    }

    /**
     * Factory method to instantiate either a JsonArrayDataSource or a
     * CsvDataSource based on input format
     * @return DataSource created from input File
     */
    static DataSourceFormat createDataSourceFormat(File input, File workDir) throws IOException {
        Path resolvedPath = DataSourceFormat.toSecure(input.toPath(), workDir.toPath());
        try (InputStream is = new FileInputStream(resolvedPath.toFile())) { // Read the file.
            return createDataSourceFormat(is);
        }
    }



    /**
     * Factory method to instantiate either a JsonArrayDataSource or a
     * CsvDataSource based on input format
     * @return DataSource created from input String
     */
    static DataSourceFormat createDataSourceFormat(InputStream input) throws IOException {
        String content = null;

        // Read the file.
        try (Reader fr = new InputStreamReader(input)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException ex) {
            throw ex;
        }

        return createDataSourceFormat(content);
    }

    //https://docs.oracle.com/javase/tutorial/essential/io/pathOps.html
    static Path toSecure(Path testPath, Path referencePath) throws IOException {
        // catch paths starting with "/" but on Windows where they get rewritten
        // to start with "\"
        if (testPath.startsWith(File.separator))
            throw new IllegalArgumentException("Input path must be relative");
        if (testPath.isAbsolute()){
            throw new IllegalArgumentException("Input path must be relative");
        }
        if (!referencePath.isAbsolute()) {
            throw new IllegalArgumentException("Reference path must be absolute");
        }
        if (testPath.toFile().isDirectory()){
            throw new IllegalArgumentException("Input path cannot be a directory");
        }
        //Path canonicalPath = testPath.toRealPath(null);
        final Path resolvedPath = referencePath.resolve(testPath).normalize();
        if (!Files.exists(resolvedPath))
            throw new FileNotFoundException("File "+resolvedPath.toString()+" does not exist");
        if (!resolvedPath.toFile().isFile()){
            throw new IllegalArgumentException("Input must be a file");
        }
        if (!resolvedPath.startsWith(referencePath)) {
            throw new IllegalArgumentException("Input path escapes the base path");
        }

        return resolvedPath;
    }
}
