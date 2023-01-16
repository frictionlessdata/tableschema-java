package io.frictionlessdata.tableschema.tabledatasource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.frictionlessdata.tableschema.Table;
import io.frictionlessdata.tableschema.exception.TableIOException;
import io.frictionlessdata.tableschema.inputstream.ByteOrderMarkStrippingInputStream;
import io.frictionlessdata.tableschema.util.JsonUtil;
import org.apache.commons.csv.CSVFormat;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
 * Interface defining the structure holding data for a {@link Table}.
 *
 * Concrete implementations implement this interface to provide ways for creating a Table on
 * <ul>
 *     <li>JSON-encoded array data</li>
 *     <li>CSV-encoded data</li>
 *     <li>Bean collections</li>
 *     <li>String arrays</li>
 * </ul>
 *
 * This class and its subclasses will strip Unicode BOMs from the input data
 */
public interface TableDataSource {
    String UTF16_BOM = "\ufeff";
    String UTF8_BOM = "\u00ef\u00bb\u00bf";

    /**
     * Returns an Iterator that returns String arrays containing
     * one row of data each.
     *
     * @return Iterator over the data
     */
    Iterator<String[]> iterator();

    /**
     * Returns the data headers if no headers were set or the set headers
     * @return Column headers as a String array
     */
    String[] getHeaders();

    /**
     * Returns the whole data as a List of String arrays, each List entry is one row
     * @return List containing the data
     *
     * @throws Exception thrown if reading the data fails
     */
    List<String[]> getDataAsStringArray() throws Exception;

    /**
     * Signals whether extracted headers can be trusted (CSV with header row) or not
     * (JSON array of JSON objects where null values are omitted).
     *
     * @return true if extracted headers can be trusted, false otherwise
     */
    boolean hasReliableHeaders();

    Charset getEncoding();

    /**
     * Factory method to instantiate either a JsonArrayDataSource or a
     * {@link CsvTableDataSource} based on input format. The method will guess
     * whether `input` is CSV or JSON data.
     *
     * @param input The text input
     * @return DataSource created from input String
     */
    static TableDataSource fromSource(String input) {
        try {
            // JSON array generation. If an exception is thrown -> probably CSV data
            ArrayNode json = JsonUtil.getInstance().createArrayNode(input);
            return new JsonArrayTableDataSource(input);
        } catch (Exception ex) {
            // JSON parsing failed, treat it as a CSV
            return new CsvTableDataSource(input);
        }
    }

    /**
     * Factory method to instantiate either a {@link JsonArrayTableDataSource} or a
     * {@link CsvTableDataSource} based on input format. The method will guess
     * whether `input` is CSV or JSON data
     *
     * @return DataSource created from input File
     */
    static TableDataSource fromSource(File input, File workDir, Charset charset) {
        try {
            Charset cs = (null == charset) ? getDefaultEncoding() : charset;
            String content = getFileContents(input.getPath(), workDir, cs);
            return fromSource(content);
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }
    }

    /**
     * Factory method to instantiate either a {@link JsonArrayTableDataSource} or a
     * {@link CsvTableDataSource} based on input format and with the default charset.
     *
     * @return DataSource created from input String
     */
    static TableDataSource fromSource(InputStream input) {
        return fromSource(input, getDefaultEncoding());
    }


    /**
     * Factory method to instantiate either a {@link JsonArrayTableDataSource} or a
     * {@link CsvTableDataSource}  based on input format
     *
     * @param charset the encoding to read data with
     * @return DataSource created from input String
     */
    static TableDataSource fromSource(InputStream input, Charset charset) {
        String content;

        Charset cs = (null == charset) ? getDefaultEncoding() : charset;
        try (Reader fr = new InputStreamReader(input, cs)) {
            try (BufferedReader rdr = new BufferedReader(fr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }

        return fromSource(content);
    }

    static String getFileContents(String path, File workDir, Charset charset) throws IOException {
        String lines;
        if (workDir.getName().endsWith(".zip")) {
            //have to exchange the backslashes on Windows, as
            //zip paths are forward slashed.
            if (File.separator.equals("\\"))
                path = path.replaceAll("\\\\", "/");
            ZipFile zipFile = new ZipFile(workDir.getAbsolutePath());
            ZipEntry entry = zipFile.getEntry(path);
            InputStream stream = zipFile.getInputStream(entry);
            lines = readSkippingBOM(stream, charset);
        } else {
            // The path value can either be a relative path or a full path.
            // If it's a relative path then build the full path by using the working directory.
            // Caution: here, we cannot simply use provided paths, we have to check
            // they are neither absolute path or relative parent paths (../)
            // see:
            //    - https://github.com/frictionlessdata/tableschema-java/issues/29
            //    - https://frictionlessdata.io/specs/data-resource/#url-or-path
            Path resolvedPath = TableDataSource.toSecure(new File(path).toPath(), workDir.toPath());
            lines = readSkippingBOM(new FileInputStream(resolvedPath.toFile()), charset);
        }
        return lines;
    }

    /**
     * Use the {@link ByteOrderMarkStrippingInputStream} class to read from the provided {@link java.io.InputStream}
     * and strip the BOM if found. Use the found BOM to determine the UTF dialect if any and read big/little endian
     * conform
     * @param is InputStream to read from
     * @return Contents of the InputStream as a String
     */
    static String readSkippingBOM(InputStream is, Charset charset) {
        String content;
        try (ByteOrderMarkStrippingInputStream bims  = new ByteOrderMarkStrippingInputStream(is);
             InputStreamReader isr = new InputStreamReader(bims.skipBOM(), charset == null ? bims.getCharset() : charset);
             BufferedReader rdr = new BufferedReader(isr)) {
                content = rdr.lines().collect(Collectors.joining("\n"));
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }
        return content;
    }

    /**
     * Return the default CSV format to use for Table data. It has the following features:
     * <ul>
     *     <li>Has a header row</li>
     *     <li>Will trim whitespace around cell content</li>
     *     <li>Will ignore empty rows</li>
     *     <li>Does not throw if duplicate column names are encountered</li>
     *     <li>Delimiter: ','</li>
     *     <li>Quote character: '"'</li>
     *     <li>Record separator": '\r\n'</li>
     * </ul>
     *
     * @return the default CSV format
     */
    static CSVFormat getDefaultCsvFormat() {
        return CSVFormat.RFC4180
                .builder()
                .setHeader()
                .setIgnoreSurroundingSpaces(true)
                .setRecordSeparator("\n")
                .build();
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

    /**
     * Get the standard {@link Charset} (encoding) to use if none is specified. According to the Datapackage
     * specs, this should not be the platform default, but UTF-8: "specify the character encoding of the
     * resource’s data file. The values should be one of the “Preferred MIME Names” for a character encoding
     * registered with IANA . If no value for this key is specified then the default is UTF-8."
     * From: https://specs.frictionlessdata.io/data-resource/#metadata-properties
     *
     * We assume the same should apply for Tables.
     *
     * @return the default Charset
     */
    public static Charset getDefaultEncoding() {
        return StandardCharsets.UTF_8;
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

    /**
     * Data format, currently either CSV or JSON. Formats like Excel are not supported
     */
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
