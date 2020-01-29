package io.frictionlessdata.tableschema.datasourceformat;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.schema.Schema;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

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
        return headers != null;
    }
}
