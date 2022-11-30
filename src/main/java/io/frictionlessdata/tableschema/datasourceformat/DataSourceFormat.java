package io.frictionlessdata.tableschema.datasourceformat;

import io.frictionlessdata.tableschema.inputstream.ByteOrderMarkStrippingInputStream;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.apache.commons.csv.CSVFormat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Interface for a source of tabular data.
 */
public interface DataSourceFormat {
    String UTF16_BOM = "\ufeff";
    String UTF8_BOM = "\u00ef\u00bb\u00bf";
    /**
     * Returns an Iterator that returns String arrays containing
     * one row of data each.
     * @return Iterator over the data
     * @throws Exception thrown if reading the data fails
     */
    Iterator<String[]> iterator() throws Exception;

    /**
     * Returns the data headers if no headers were set or the set headers
     * @return Column headers as a String array
     */
    String[] getHeaders() throws Exception;

    /**
     * Returns the whole data as a List of String arrays, each List entry is one row
     * @return List containing the data
     * @throws Exception thrown if reading the data fails
     */
    List<String[]> data() throws Exception;

    /**
     * Signals whether extracted headers can be trusted (CSV with header row) or not
     * (JSON array of JSON objects where null values are omitted).
     * @return true if extracted headers can be trusted, false otherwise
     */
    boolean hasReliableHeaders();

    /**
     * Factory method to instantiate either a JsonArrayDataSource or a
     * CsvDataSource based on input format
     * @return DataSource created from input String
     */
    static DataSourceFormat createDataSourceFormat(String input) {
        try {
            // JSON array generation only to see if an exception is thrown -> probably CSV data
            JsonUtil.getInstance().createArrayNode(input);
            return new JsonArrayDataSourceFormat(input);
        } catch (Exception ex) {
            // JSON parsing failed, treat it as a CSV
            return new CsvDataSourceFormat(input);
        }
    }

    /**
     * Factory method to instantiate either a {@link JsonArrayDataSourceFormat} or a
     * {@link CsvDataSourceFormat} based on input format
     * @return DataSource created from input File
     */
    static DataSourceFormat createDataSourceFormat(File input, File workDir) throws IOException {
        String content = getFileContents(input.getPath(), workDir);
        return createDataSourceFormat(content);
    }

    static String getFileContents(String path, File workDir) throws IOException {
        String lines;
        if (workDir.getName().endsWith(".zip")) {
            //have to exchange the backslashes on Windows, as
            //zip paths are forward slashed.
            if (File.separator.equals("\\"))
                path = path.replaceAll("\\\\", "/");
            ZipFile zipFile = new ZipFile(workDir.getAbsolutePath());
            ZipEntry entry = zipFile.getEntry(path);
            InputStream stream = zipFile.getInputStream(entry);
            lines = readSkippingBOM(stream);
        } else {
            // The path value can either be a relative path or a full path.
            // If it's a relative path then build the full path by using the working directory.
            // Caution: here, we cannot simply use provided paths, we have to check
            // they are neither absolute path or relative parent paths (../)
            // see:
            //    - https://github.com/frictionlessdata/tableschema-java/issues/29
            //    - https://frictionlessdata.io/specs/data-resource/#url-or-path
            Path resolvedPath = DataSourceFormat.toSecure(new File(path).toPath(), workDir.toPath());
            lines = readSkippingBOM(new FileInputStream(resolvedPath.toFile()));
        }
        return lines;
    }

    /**
     * Use the {@link ByteOrderMarkStrippingInputStream} class to read from the provided {@link java.io.InputStream}
     * and strip the BOM if found. Use the found BOM to determine the UTF dialect if any and read big/little endian
     * conform
     * @param is InputStream to read from
     * @return Contents of the InputStream as a String
     * @throws IOException if underlying InputStream throws
     */
    static String readSkippingBOM(InputStream is) throws IOException {
        String content;
        try (ByteOrderMarkStrippingInputStream bims  = new ByteOrderMarkStrippingInputStream(is);
             InputStreamReader isr = new InputStreamReader(bims.skipBOM(), bims.getCharset());
             BufferedReader rdr = new BufferedReader(isr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
        }
        return content;
    }

    static CSVFormat getDefaultCsvFormat() {
        return CSVFormat.RFC4180
                .builder()
                .setHeader()
                .setIgnoreSurroundingSpaces(true)
                .setRecordSeparator("\n")
                .build();
    }

    /**
     * Factory method to instantiate either a {@link JsonArrayDataSourceFormat} or a
     * {@link CsvDataSourceFormat}  based on input format
     * @return DataSource created from input String
     */
    static DataSourceFormat createDataSourceFormat(InputStream input) throws IOException {
        String content;

        // Read the file.
        try (Reader fr = new InputStreamReader(input)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        }

        return createDataSourceFormat(content);
    }

    static String trimBOM(String input) {
        if (null == input)
            return null;
        if( input.startsWith(UTF16_BOM)) {
            input = input.substring(1);
        } else if( input.startsWith(UTF8_BOM)) {
            input = input.substring(3);
        }
        return input;
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
        final Path resolvedPath = referencePath.resolve(testPath).normalize();
        if (!Files.exists(resolvedPath))
            throw new FileNotFoundException("File "+resolvedPath+" does not exist");
        if (!resolvedPath.toFile().isFile()){
            throw new IllegalArgumentException("Input must be a file");
        }
        if (!resolvedPath.startsWith(referencePath)) {
            throw new IllegalArgumentException("Input path escapes the base path");
        }

        return resolvedPath;
    }

    enum Format {
        FORMAT_CSV("csv"),
        FORMAT_JSON("json");

        private static final Map<String, Format> lookup = new HashMap<>();
        private final String label;

        Format(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static Format byName(String label) {
            return lookup.get(label);
        }

        /*
            Populate lookup dict at load time
         */

        static {
            for (Format env : Format.values()) {
                lookup.put(env.getLabel(), env);
            }
        }
    }
}
