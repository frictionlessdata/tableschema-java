package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.exception.TableValidationException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.datasourceformats.CsvDataSourceFormat;
import io.frictionlessdata.tableschema.datasourceformats.DataSourceFormat;
import io.frictionlessdata.tableschema.exception.InvalidCastException;

import java.io.*;

import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.iterator.SimpleTableIterator;
import io.frictionlessdata.tableschema.iterator.TableIterator;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.schema.TypeInferrer;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;
import org.apache.commons.csv.CSVFormat;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents a CSV table
 * 
 */
public class Table{
    private DataSourceFormat dataSourceFormat = null;
    private Schema schema = null;
    private CSVFormat format = DataSourceFormat.getDefaultCsvFormat();
    private Map<String, Object> fieldOptions;

    /**
     * Constructor using either a CSV or JSON array-containing string.
     * @param dataSource the CSV or JSON content for the Table
     */
    public Table(String dataSource) {
        this.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource);
    }

    /**
     * Constructor using an {@link java.io.InputStream} for reading both the CSV
     * data and the table schema.
     * @param dataSource InputStream for reading the CSV from
     * @param schema InputStream for reading table schema from
     * @throws Exception if either reading or parsing throws an Exception
     */
    public Table(InputStream dataSource, InputStream schema, CSVFormat format) throws Exception{
        this.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource);
        this.schema = Schema.fromJson(schema, true);
        if (null != format) {
            setCsvFormat(format);
        }
    }

    public Table(File dataSource, File basePath) throws Exception{
        this.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource, basePath);
    }

    public Table(File dataSource, File basePath, Schema schema, CSVFormat format) throws Exception{
        this(dataSource, basePath);
        this.schema = schema;
        if (null != format) {
            setCsvFormat(format);
        }
    }

    public Table(String dataSource, Schema schema, CSVFormat format) {
        this.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource);
        this.schema = schema;
        if (null != format) {
            setCsvFormat(format);
        }
    }

    public Table(URL dataSource) throws IOException {
        this.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource.openStream());
    }

    public Table(URL dataSource, Schema schema, CSVFormat format) throws IOException {
        this(dataSource);
        this.schema = schema;
        if (null != format) {
            setCsvFormat(format);
        }
    }
    
    public Table(URL dataSource, URL schema) throws Exception{
        this(dataSource, Schema.fromJson(schema, true), null);
    }

    public Iterator<Object[]> iterator() throws Exception{
       return new TableIterator<>(this, false, false, true, false);
    }

    public Iterator<Object[]> iterator(boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception{
       return new TableIterator<>(this, keyed, extended, cast, relations);
    }

    public Iterator<String[]> stringArrayIterator() throws Exception{
        return new SimpleTableIterator(this, false);
    }

    public Iterator<String[]> stringArrayIterator(boolean relations) throws Exception{
        return new SimpleTableIterator(this, relations);
    }

    public Iterator<Map> keyedIterator() throws Exception{
        return new TableIterator<Map>(this, true, false, true, false);
    }

    public Iterator<Map> keyedIterator(boolean extended, boolean cast, boolean relations) throws Exception{
        return new TableIterator<Map>(this, true, extended, cast, relations);
    }

    public Map<Integer, Integer> getSchemaHeaderMapping() {
        try {
            if (null == schema) {
                return TableSchemaUtil
                        .createSchemaHeaderMapping(dataSourceFormat.getHeaders(), dataSourceFormat.getHeaders());
            } else {
                return TableSchemaUtil
                        .createSchemaHeaderMapping(dataSourceFormat.getHeaders(), getDeclaredHeaders());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns either the headers in the order declared in the Schema or in the order found in
     * the data if no Schema has been set. In the case where we don't have a Schema, the order
     * is only well-defined for Tables read from CSV.
     *
     * If the input source is a JSON array of JSON objects, the order of columns is arbitrary:
     *
     * "An object is an unordered set of name/value pairs"
     * (https://www.json.org/json-en.html)
     *
     * @return an array of header names
     * @throws Exception if reading headers from the input source raises an exception
     */
    public String[] getHeaders() throws Exception{
        if (null != schema) {
            return getDeclaredHeaders();
        }
        return this.dataSourceFormat.getHeaders();
    }

    String[] getDeclaredHeaders() {
        if (null == schema)
            return null;

        return schema
                .getFields()
                .stream()
                .map(Field::getName)
                .toArray(String[]::new);
    }

    public List<Object[]> read(boolean cast) throws Exception{
        if(cast && (null == schema)){
            throw new TableSchemaException("Cannot cast without a schema");
        }
        if(cast && !this.schema.hasFields()){
            throw new InvalidCastException("Schema has no fields");
        }
        
        List<Object[]> rows = new ArrayList();
        
        Iterator<Object[]> iter = this.iterator(false, false, cast, false);
        while(iter.hasNext()){
            Object[] row = iter.next();
            rows.add(row);
        }

        return rows;
    }
    
    public List<Object[]> read() throws Exception{
        boolean cast =  (null != schema);
        return this.read(cast);
    }

    public void writeCsv(Writer out, CSVFormat format) {
        try {
            String[] headers = null;
            if (null != schema) {
                List<String> fieldNames = schema.getFieldNames();
                headers = fieldNames.toArray(new String[fieldNames.size()]);
            } else {
                headers = dataSourceFormat.getHeaders();
            }
            dataSourceFormat.writeCsv(out, format, headers);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeCsv(File outputFile, CSVFormat format) throws Exception{
        try (FileWriter fw = new FileWriter(outputFile)) {
            writeCsv(fw, format);
        }
    }

    /**
     * Validates that names of the headers are as declared in the Schema, and
     * throws a TableValidationException if they aren't. If the headers derived from the
     * DataSourceFormat aren't reliable (eg. JSON array of JSON objects where properties
     * that are `null` would be omitted), then we don't test whether all declared header
     * names are present.
     *
     * Sort order is neglected to allow a Schema to define column order. This is intentional,
     * as JSON-objects do not have a sort order of their keys. Therefore, reading not from
     * a CSV but a JSON array of JSON objects needs this flexibility.
     *
     * @throws TableValidationException thrown if the header names do not match the
     *          fields declared in the schema
     * @throws TableSchemaException thrown if something goes wrong retrieving the table headers
     */
    public void validate() throws TableValidationException, TableSchemaException {
        if (null == schema)
            return;
        String[] headers = null;
        try {
            headers = this.dataSourceFormat.getHeaders();
        } catch (Exception ex) {
            throw new TableSchemaException(ex);
        }
        List<String> declaredHeaders = Arrays.asList(getDeclaredHeaders());
        List<String> foundHeaders = Arrays.asList(headers);
        if (dataSourceFormat.hasReliableHeaders()) {
            for (String col : declaredHeaders) {
                if (!foundHeaders.contains(col)) {
                    throw new TableValidationException("Declared column " + col + " not found in data");
                }
            }
        }
        for (String col : headers) {
            if (!declaredHeaders.contains(col)) {
                throw new TableValidationException("Found undeclared column: "+col);
            }
        }

    }
    
    public Schema inferSchema() throws TypeInferringException{
        return inferSchema(-1);
    }
    
    public Schema inferSchema(int rowLimit) throws TypeInferringException{
        try{
            List<Object[]> data = read();
            String[] headers = getHeaders();
            schema = Schema.infer(data, headers, rowLimit);
            return schema;
            
        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    public Table setCsvFormat(CSVFormat format) {
        this.format = format;
        if ((null != dataSourceFormat) && (dataSourceFormat instanceof CsvDataSourceFormat)) {
            ((CsvDataSourceFormat) dataSourceFormat).format(format);
        }
        return this;
    }

    public void setFieldOptions(Map<String, Object> options) {
        this.fieldOptions = options;
    }

    public Map<String, Object> getFieldOptions() {
        return fieldOptions;
    }

    public CSVFormat getCsvFormat() {
        return format;
    }

    public Schema schema(){
        return this.schema;
    }
    
    public DataSourceFormat dataSource(){
        return this.dataSourceFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        boolean equals = false;
        try {
            equals = Arrays.equals(table.getHeaders(), ((Table) o).getHeaders());
            equals = equals & table.inferSchema(10).equals(((Table) o).inferSchema(10));
            List<Object[]> data = table.read();
            List<Object[]> oData = ((Table) o).read();
            equals = equals & data.size() == oData.size();
            for (int i = 0; i <data.size(); i++) {
                equals = equals & Arrays.equals(data.get(i), oData.get(i));
            }
            if (equals)
                return true;
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSourceFormat, schema, format);
    }

}