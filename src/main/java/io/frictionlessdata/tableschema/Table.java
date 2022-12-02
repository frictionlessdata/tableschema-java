package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.datasourceformat.CsvDataSourceFormat;
import io.frictionlessdata.tableschema.datasourceformat.DataSourceFormat;
import io.frictionlessdata.tableschema.datasourceformat.StringArrayDataSourceFormat;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.exception.TableValidationException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.iterator.BeanIterator;
import io.frictionlessdata.tableschema.iterator.SimpleTableIterator;
import io.frictionlessdata.tableschema.iterator.TableIterator;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.util.JsonUtil;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * This class represents a CSV or JSON-array encoded  table with optional CSV specification
 * via a {@link org.apache.commons.csv.CSVFormat} and optional provable assurances towards format integrity via a Table
 * {@link io.frictionlessdata.tableschema.schema.Schema}.
 *
 * This class makes a semantic difference between constructors and the overloaded factory method {@link #fromSource}.
 * constructors are intended for capturing new data, while the `fromSource()` method is intended for reading existing
 * CSV or JSON data.
 *
 * Reading data from a Table instance is done via a {@link io.frictionlessdata.tableschema.iterator.TableIterator},
 * which can be configured to return table rows as:
 *<ul>
 * <li> String arrays</li>
 * <li> as Object arrays (parameter `cast` = true)</li>
 * <li> as a Map&lt;key,val&gt; where key is the header name, and val is the data (parameter `keyed` = true)</li>
 * <li> or in an "extended" form (parameter `extended` = true) that returns an Object array where the first entry is the
 *      row number, the second is a String array holding the headers, and the third is an Object array holding
 *      the row data.</li>
 *</ul>
 *  Roughly implemented after https://github.com/frictionlessdata/tableschema-py/blob/master/tableschema/table.py
 */
public class Table{
    private DataSourceFormat dataSourceFormat = null;
    private Schema schema = null;
    private CSVFormat format = DataSourceFormat.getDefaultCsvFormat();

    /**
     * Constructor for an empty Table. It contains neither data nor is it controlled by a Schema
     */
    public Table() { }

    /**
     * Constructor for a Table from String array input data and optionally
     * a Schema. Data is parsed into a DataSourceFormat object
     * @param data a {@link java.util.Collection} holding rows of data encoded as String-arrays
     * @param headers the column header names for writing out as CSV/JSON
     * @param schema Table Schema to control the format of the data. Can be null.
     */
    public Table(Collection<String[]> data, String[] headers, Schema schema) {
        this.dataSourceFormat = new StringArrayDataSourceFormat(data, headers);
        this.schema = schema;
        if (null != schema)
            validate();
    }

    /**
     * Create Table on an {@link java.io.InputStream} for reading both the CSV/JSON
     * data and the table schema.
     * @param dataSource InputStream for reading the data from
     * @param schema InputStream for reading table schema from
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data
     * @throws Exception if either reading or parsing throws an Exception
     */
    public static Table fromSource(InputStream dataSource, InputStream schema, CSVFormat format) throws Exception{
        Table table = new Table();
        table.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource);
        if (null != schema) {
            table.schema = Schema.fromJson(schema, true);
        }
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * Create Table from a {@link java.io.File} containing the CSV/JSON
     * data and without either a Schema or a CSVFormat.
     * @param dataSource InputStream for reading the data from
     * @throws Exception if either reading or parsing throws an Exception
     */
    public static Table fromSource(File dataSource, File basePath) throws Exception{
        Table table = new Table();
        table.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource, basePath);
        return table;
    }

    /**
     * Create Table from a {@link java.io.File} containing the CSV/JSON
     * data and with  a Schema and a CSVFormat.
     * @param dataSource InputStream for reading the data from
     * @param schema InputStream for reading table schema from
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data
     * @throws Exception if either reading or parsing throws an Exception
     */
    public static Table fromSource(File dataSource, File basePath, Schema schema, CSVFormat format) throws Exception{
        Table table = fromSource(dataSource, basePath);
        table.schema = schema;
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * Create Table using either a CSV or JSON array-containing string and without either a Schema or a CSVFormat.
     * @param dataSource the CSV or JSON content for the Table
     */
    public static Table fromSource(String dataSource) {
        Table table = new Table();
        table.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource);
        return table;
    }

    /**
     * Create Table using either a CSV or JSON array-containing string and with  a Schema and a CSVFormat.
     * @param dataSource the CSV or JSON content for the Table
     * @param schema table schema
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data
     */
    public static Table fromSource(String dataSource, Schema schema, CSVFormat format) {
        Table table = new Table();
        table.schema = schema;
        table.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource);
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * Create Table from a URL containing either CSV or JSON and without either a Schema or a CSVFormat.
     * @param dataSource the URL for the CSV or JSON content
     * @throws IOException if reading throws an Exception
     */
    public static Table fromSource(URL dataSource) throws IOException {
        Table table = new Table();
        table.dataSourceFormat = DataSourceFormat.createDataSourceFormat(dataSource.openStream());
        return table;
    }


    /**
     * Create Table from a URL containing either CSV or JSON and with  a Schema and a CSVFormat.
     * @param dataSource the URL for the CSV or JSON content
     * @param schemaUrl the URL for the table schema. Can be null
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data
     * @throws IOException if reading throws an Exception
     */
    public static Table fromSource(URL dataSource, URL schemaUrl, CSVFormat format) throws Exception {
        Schema schema = null;
        if (null != schemaUrl) {
            schema = Schema.fromJson(schemaUrl, true);
        }
        return fromSource(dataSource, schema, format);
    }

    /**
     * Create Table from a URL containing either CSV or JSON and with  a Schema and a CSVFormat.
     * @param dataSource the URL for the CSV or JSON content
     * @param schema table schema
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data
     * @throws IOException if reading throws an Exception
     */
    public static Table fromSource(URL dataSource, Schema schema, CSVFormat format) throws IOException {
        Table table = fromSource(dataSource);
        table.schema = schema;
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * This is the simplest case to read data from a Table referencing a file or URL.
     *
     * Each row of the table will be returned as an Object array. Values in each column
     * are parsed and converted ("cast) to Java objects on a best guess approach.
     * @return Interator returning table rows as Object Arrays
     * @throws Exception if parsing the data fails
     */
    public Iterator<Object[]> iterator() throws Exception{
       return new TableIterator<>(this, false, false, true, false);
    }

    /**
     * This is the most flexible way to read data from a Table referencing a file or URL.Each row of the table
     * will be returned as an Object array. Options allow you to tailor the behavior of the Iterator to your needs:
     *  *<ul>
     *  * <li> String arrays (parameter `cast` = false)</li>
     *  * <li> as Object arrays (parameter `cast` = true)</li>
     *  * <li> as a Map&lt;key,val&gt; where key is the header name, and val is the data (parameter `keyed` = true)</li>
     *  * <li> or in an "extended" form (parameter `extended` = true) that returns an Object array where the first entry is the
     *  *      row number, the second is a String array holding the headers, and the third is an Object array holding
     *  *      the row data.</li>
     *  * <li> Resolving references to other data sources (parameter `relations` = true)</li>
     *  *</ul>
     *
     * Values in each column
     * are parsed and converted ("cast) to Java objects on a best guess approach.
     * @return Interator returning table rows as Object Arrays
     * @throws Exception if parsing the data fails
     */
    public Iterator<Object[]> iterator(boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception{
       return new TableIterator<>(this, keyed, extended, cast, relations);
    }

    /**
     * This method creates an Iterator that will return table rows as Java objects of the type `beanClass`.
     * It therefore disregards the Schema set on the table but creates its own Schema from the supplied `beanType`.
     *
     * @param beanType the Bean class this BeanIterator expects
     * @param relations Whether references to other data sources get resolved
     * @return Iterator that returns rows as bean instances.
     */
    public BeanIterator<?> iterator(Class<?> beanType, boolean relations) throws Exception{
        return new BeanIterator(this,  beanType, relations);
    }

    public Iterator<String[]> stringArrayIterator() throws Exception{
        return new SimpleTableIterator(this, false);
    }

    public Iterator<String[]> stringArrayIterator(boolean relations) throws Exception{
        return new SimpleTableIterator(this, relations);
    }

    public Iterator<Map<String, Object>> keyedIterator() throws Exception{
        return new TableIterator<>(this, true, false, true, false);
    }

    public Iterator<Map<String, Object>> keyedIterator(boolean extended, boolean cast, boolean relations) throws Exception{
        return new TableIterator<>(this, true, extended, cast, relations);
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
            if (ex instanceof RuntimeException)
                throw (RuntimeException)ex;
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns either the headers in the order declared in the Schema or in the order found in
     * the data if no Schema has been set. In the case where we don't have a Schema, the order
     * is only well-defined for Tables read from CSV.
     *
     * If the input source is a JSON array of JSON objects, the order of columns is arbitrary, as JSON objects do
     * not preserve key order:
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

    private String[] getDeclaredHeaders() {
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
        
        List<Object[]> rows = new ArrayList<>();
        
        Iterator<Object[]> iter = this.iterator(false, false, cast, false);
        while(iter.hasNext()){
            Object[] row = iter.next();
            rows.add(row);
        }

        return rows;
    }
    
    public List<Object[]> read() throws Exception{
        boolean cast = (null != schema);
        return read(cast);
    }

    public String asJson() {
        try {
            List<Map<String, Object>> arr = new ArrayList<>();
            List<Object[]> records = read();
            Schema schema = (null != this.schema) ? this.schema : this.inferSchema();
            for (Object[] rec : records) {
                Map<String, Object> obj = new HashMap<>();
                int i = 0;
                for (Field field : schema.getFields()) {
                    Object s = rec[i];
                    obj.put(field.getName(), field.formatValueForJson(s));
                    i++;
                }
                arr.add(obj);
            }
            return JsonUtil.getInstance().serialize(arr);
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                throw (RuntimeException)ex;
            throw new RuntimeException(ex);
        }
    }

    private void writeCSVData(Map<Integer, Integer> mapping, CSVPrinter csvPrinter) {
        try {
            this.iterator(false, false, false, false).forEachRemaining((record) -> {
                String[] sortedRec = new String[record.length];
                for (int i = 0; i < record.length; i++) {
                    sortedRec[mapping.get(i)] = (String)record[i];
                }
                try {
                    csvPrinter.printRecord(sortedRec);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                throw (RuntimeException)ex;
            throw new RuntimeException(ex);
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
    //@Override
    private void writeCsv(Writer out, CSVFormat format, String[] sortedHeaders) {
        try {
            if (null == sortedHeaders) {
                writeCsv(out, format, getHeaders());
                return;
            }
            CSVFormat locFormat = (null != format)
                    ? format
                    : DataSourceFormat.getDefaultCsvFormat();

            locFormat = locFormat.builder().setHeader(sortedHeaders).build();
            CSVPrinter csvPrinter = new CSVPrinter(out, locFormat);

            String[] headers = getHeaders();
            Map<Integer, Integer> mapping
                    = TableSchemaUtil.createSchemaHeaderMapping(headers, sortedHeaders);
            writeCSVData( mapping, csvPrinter);
            csvPrinter.close();
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                throw (RuntimeException)ex;
            throw new RuntimeException(ex);
        }
    }

    /**
     * Write Table data to a provided {@link java.io.Writer} - the `dataFormat` parameter decides on the data format,
     * either CSV or JSON.
     * @param out the Writer to write to
     * @param dataFormat the format to use, either CSV or JSON.
     */
    public void write(Writer out, DataSourceFormat.Format dataFormat) {
        try  {
            if (dataFormat.equals(DataSourceFormat.Format.FORMAT_CSV)) {
                try {
                    String[] headers;
                    if (null != schema) {
                        List<String> fieldNames = schema.getFieldNames();
                        headers = fieldNames.toArray(new String[0]);
                    } else {
                        headers = dataSourceFormat.getHeaders();
                    }
                    writeCsv(out, this.format, headers);
                } catch (Exception ex) {
                    if (ex instanceof RuntimeException)
                        throw ex;
                    throw new RuntimeException(ex);
                }
            } else if (dataFormat.equals(DataSourceFormat.Format.FORMAT_JSON)) {
                String content = this.asJson();
                out.write(content);
            }
        } catch (Exception ex) {
            if (ex instanceof RuntimeException)
                throw (RuntimeException)ex;
            throw new RuntimeException(ex);
        }
    }

    /**
     * Write Table data as CSV to a provided {@link java.io.Writer}, the `format` parameter decides on the CSV
     * options. If it is null, then the data will be written in the CSV defined by the CSV format of the Table
     *
     * @param out the Writer to write to
     * @param format the CSV format to use
     */
    public void writeCsv(Writer out, CSVFormat format) {
        CSVFormat oldFormat = this.format;
        if (null != format) {
            this.format = format;
        }
        write(out, DataSourceFormat.Format.FORMAT_CSV);
        this.format = oldFormat;
    }

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is null, then the data will
     * be written in the CSV defined by the CSV format of the Table
     *
     * @param outputFile the File to write to
     * @param format the CSV format to use
     */
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
     * names are present. Likewise, if the CSVFormat used doesn't specify a header row
     * (e.g. CSVFormat.DEFAULT), then stop validation.
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
        String[] headers;
        try {
            headers = this.dataSourceFormat.getHeaders();
        } catch (Exception ex) {
            throw new TableSchemaException(ex);
        }
        if (null == headers) {
            return;
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
            return inferSchema(getHeaders(),rowLimit);
        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    public Schema inferSchema(String[] headers, int rowLimit) throws TypeInferringException{
        try{
            List<Object[]> data = read();
            schema = Schema.infer(data, headers, rowLimit);
            return schema;

        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }


    public Table setCsvFormat(CSVFormat format) {
        this.format = format;
        if ((null != dataSourceFormat) && (dataSourceFormat instanceof CsvDataSourceFormat)) {
            ((CsvDataSourceFormat) dataSourceFormat).setFormat(format);
        }
        return this;
    }

    public CSVFormat getCsvFormat() {
        return format;
    }

    /**
     * Get the current Schema for this Table
     * @return the active Schema
     */
    public Schema getSchema(){
        return this.schema;
    }

    /**
     * Set a Schema for this Table. If the Table is connected to a DataSourceFormat, ie. holds data,
     * then the data will be validated against the new Schema.
     * @param schema the Schema to set
     */
    public Table setSchema(Schema schema) {
        this.schema = schema;
        if (null != dataSourceFormat)
            validate();
        return this;
    }

    /**
     * Get the current DataSourceFormat for this Table
     * @return the active DataSourceFormat
     */
    public DataSourceFormat getDataSourceFormat(){
        return this.dataSourceFormat;
    }

    /**
     * Set a DataSourceFormat for this Table, ie. set the data for the Table. If this Table has an active Schema,
     * then the data will be validated against the Schema.
     * @param fmt the DataSourceFormat to set
     */
    public Table setDataSourceFormat(DataSourceFormat fmt) {
        this.dataSourceFormat = fmt;
        if (null != schema)
            validate();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        boolean equals;
        try {
            equals = Arrays.equals(table.getHeaders(), ((Table) o).getHeaders());
            if ((this.schema != null) && (table.schema != null)) {
                equals = equals & this.schema.equals(table.schema);
            } else {
                equals = equals & table.inferSchema(10).equals(((Table) o).inferSchema(10));
            }
            List<Object[]> data = table.read();
            List<Object[]> oData = ((Table) o).read();
            equals = equals & data.size() == oData.size();
            Iterator<Object[]> iterator = this.iterator(false, false, false, true);
            Iterator<Object[]> oIter = ((Table) o).iterator(false, false, false, true);
            while (iterator.hasNext()) {
                Object[] arr = iterator.next();
                Object[] oArr = oIter.next();
                equals = equals & Arrays.equals(arr, oArr);
            }
            return equals;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSourceFormat, schema, format);
    }

}