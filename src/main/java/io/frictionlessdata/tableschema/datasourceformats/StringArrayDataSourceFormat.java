package io.frictionlessdata.tableschema.datasourceformats;

import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
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
public class StringArrayDataSourceFormat extends AbstractDataSourceFormat {
    private  String[] headers;

    public StringArrayDataSourceFormat(Collection<String[]> data, String[] headers){
        this.dataSource = data;
        this.headers = headers;
    }


    CSVParser getCSVParser() {
        throw new TableSchemaException("Not implemented for StringArrayDataSourceFormat");
    };

    @Override
    public Iterator<String[]> iterator() throws Exception{
        return ((Collection<String[]>)dataSource).iterator();
    }

    @Override
    public List<String[]> data() throws Exception{
        return new ArrayList<>((Collection<String[]>)dataSource);
    }


    @Override
    public String[] getHeaders() throws Exception{
        return headers;
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
