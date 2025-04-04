package io.frictionlessdata.tableschema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.frictionlessdata.tableschema.exception.*;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.iterator.BeanIterator;
import io.frictionlessdata.tableschema.iterator.SimpleTableIterator;
import io.frictionlessdata.tableschema.iterator.TableIterator;
import io.frictionlessdata.tableschema.schema.BeanSchema;
import io.frictionlessdata.tableschema.schema.Schema;
import io.frictionlessdata.tableschema.tabledatasource.BeanTableDataSource;
import io.frictionlessdata.tableschema.tabledatasource.CsvTableDataSource;
import io.frictionlessdata.tableschema.tabledatasource.StringArrayTableDataSource;
import io.frictionlessdata.tableschema.tabledatasource.TableDataSource;
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
 * Holding data is delegated to an instance of a {@link TableDataSource}, which has subclasses tailored to reading
 * data from CSV, JSON or String Arrays. Reading and optionally parsing data from String to Java objects is
 * delegated to one of the Iterator classes.
 *
 * This class makes a semantic difference between constructors and the overloaded factory method {@link #fromSource}.
 * constructors are intended for capturing new data, while the `fromSource()` method is intended for reading existing
 * CSV or JSON data.
 *
 * Reading data from a Table instance is done via a {@link io.frictionlessdata.tableschema.iterator.TableIterator},
 * which can be configured to return table rows as:
 *<ul>
 * <li> Java objects if you supply a Bean class to the iterator. Each row will be converted to one instance
 *      of the Bean class</li>
 * <li> String arrays (parameter `cast` = false)</li>
 * <li> as Object arrays (parameter `cast` = true)</li>
 * <li> as a Map&lt;String,Object&gt; where key is the header name, and val is the data (parameter `keyed` = true)</li>
 * <li> or in an "extended" form (parameter `extended` = true) that returns an Object array where the first entry is the
 *      row number, the second is a String array holding the headers, and the third is an Object array holding
 *      the row data.</li>
 *</ul>
 *  Roughly implemented after https://github.com/frictionlessdata/tableschema-py/blob/master/tableschema/table.py
 */
public class Table{
    private TableDataSource dataSource = null;
    private Schema schema = null;
    private CSVFormat format = TableDataSource.getDefaultCsvFormat();

    /**
     * Constructor for an empty Table. It contains neither data nor is it controlled by a Schema
     */
    public Table() { }

    /**
     * Constructor for a Table from a Collection of Java Beans and the Bean class.
     * @param data a {@link java.util.Collection} holding Java Beans of type `type`
     * @param type the Bean class
     */
    public Table(Collection<?> data, Class<?> type) {
        BeanTableDataSource ds = new BeanTableDataSource(data, type);
        this.dataSource = ds;
        schema = BeanSchema.infer(ds.getBeanClass());
        validate();
    }

    /**
     * Constructor for a Table from String array input data and optionally
     * a Schema. Data is parsed into a TableDataSource object
     * @param data a {@link java.util.Collection} holding rows of data encoded as String-arrays
     * @param headers the column header names for writing out as CSV/JSON
     * @param schema Table Schema to control the format of the data. Can be null.
     */
    public Table(Collection<String[]> data, String[] headers, Schema schema) {
        this.dataSource = new StringArrayTableDataSource(data, headers);
        this.schema = schema;
        if (null != schema)
            validate();
    }

    /**
     * Constructor for a Table from a {@link BeanTableDataSource} instance holding a collection
     * of instances of a certain Bean class. The Schema is inferred from the Bean class.
     * @param dataSource the input data
     */
    public static Table fromSource(BeanTableDataSource dataSource) {
        Table table = new Table();
        table.dataSource = dataSource;
        table.schema = BeanSchema.infer(dataSource.getBeanClass());
        return table;
    }

    /**
     * Create Table on an {@link java.io.InputStream} for reading both the CSV/JSON
     * data and the table schema.
     * @param data InputStream for reading the data from
     * @param schema InputStream for reading table schema from. Can be `null`
     * @param format The expected CSVFormat if data is a CSV-containing InputStream; ignored for JSON data.
     *               Can be `null`
     */
    public static Table fromSource(InputStream data, InputStream schema, CSVFormat format){
        Table table = new Table();
        table.dataSource = TableDataSource.fromSource(data);
        if (null != schema) {
            try {
                table.schema = Schema.fromJson(schema, true);
            } catch (IOException ex) {
                throw new TableIOException(ex);
            }
        }
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * Create Table from a {@link java.io.File} containing the CSV/JSON
     * data and with  a Schema and a CSVFormat.
     * @param dataSource relative File for reading the data from. Must be inside `basePath`
     * @param basePath Parent directory
     * @param schema The table Schema. Can be `null`
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data.
     *               Can be `null`
     */
    public static Table fromSource(File dataSource, File basePath, Schema schema, CSVFormat format) {
        Table table = fromSource(dataSource, basePath);
        table.schema = schema;
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * Create Table from a {@link java.io.File} containing the CSV/JSON
     * data and without either a Schema or a CSVFormat.
     * @param dataSource relative File for reading the data from. Must be inside `basePath`
     * @param basePath Parent directory
     */
    public static Table fromSource(File dataSource, File basePath) {
        Table table = new Table();
        table.dataSource = TableDataSource.fromSource(dataSource, basePath);
        return table;
    }

    /**
     * Create Table using either a CSV or JSON array-containing string and without either a Schema or a CSVFormat.
     * @param data the CSV or JSON content for the Table
     */
    public static Table fromSource(String data) {
        Table table = new Table();
        table.dataSource = TableDataSource.fromSource(data);
        return table;
    }

    /**
     * Create Table using either a CSV or JSON array-containing string and with  a Schema and a CSVFormat.
     * @param data the CSV or JSON content for the Table
     * @param schema table schema. Can be `null`
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data.
     *               Can be `null`
     */
    public static Table fromSource(String data, Schema schema, CSVFormat format) {
        Table table = new Table();
        table.schema = schema;
        table.dataSource = TableDataSource.fromSource(data);
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * Create Table from a URL containing either CSV or JSON and without either a Schema or a CSVFormat.
     * @param dataSource the URL for the CSV or JSON content
     */
    public static Table fromSource(URL dataSource)  {
        try {
            Table table = new Table();
            table.dataSource = TableDataSource.fromSource(dataSource.openStream());
            return table;
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }
    }


    /**
     * Create Table from a URL containing either CSV or JSON and with  a Schema and a CSVFormat.
     * @param dataSource the URL for the CSV or JSON content
     * @param schemaUrl the URL for the table schema. Can be null
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data.
     *               Can be `null`
     */
    public static Table fromSource(URL dataSource, URL schemaUrl, CSVFormat format) {
        try {
            Schema schema = null;
            if (null != schemaUrl) {
                schema = Schema.fromJson(schemaUrl, true);
            }
            return fromSource(dataSource, schema, format);
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }
    }

    /**
     * Create Table from a URL containing either CSV or JSON and with  a Schema and a CSVFormat.
     * @param dataSource the URL for the CSV or JSON content
     * @param schema table schema. Can be `null`
     * @param format The expected CSVFormat if dataSource is a CSV-containing InputStream; ignored for JSON data.
     *               Can be `null`
     */
    public static Table fromSource(URL dataSource, Schema schema, CSVFormat format) {
        Table table = fromSource(dataSource);
        table.schema = schema;
        if (null != format) {
            table.setCsvFormat(format);
        }
        return table;
    }

    /**
     * This method creates an Iterator that will return table rows as Java objects of the type `beanClass`.
     * It therefore disregards the Schema set on the table but creates its own Schema from the supplied `beanType`.
     *
     * @param beanType the Bean class this BeanIterator expects
     * @param relations Whether references to other data sources get resolved
     * @return Iterator that returns rows as bean instances.
     */
    public BeanIterator<?> iterator(Class<?> beanType, boolean relations){
        return new BeanIterator(this,  beanType, relations);
    }

    /**
     * This is the simplest case to read Object data from a Table referencing a file or URL.
     *
     * If a Schema is set on a table, each row will be returned as an Object array. Values in each column
     * are parsed and converted ("cast") to Java objects based on the Field definitions of the Schema. If no Schema is
     * present, rows will always return string arrays
     *
     * @return Iterator returning table rows as Object/String Arrays
     */
    public Iterator<Object[]> iterator() {
        return new TableIterator<>(this, false, false, true, false);
    }

    /**
     * This is the most flexible way to read data from a Table referencing a file or URL. Each row of the table
     * will be returned as an either an Object array or a Map&lt;String, Object&gt;, depending on Options.
     * Options allow you to tailor the behavior of the Iterator to your needs:
     *  <ul>
     *      <li> String arrays (parameter `cast` = false)</li>
     *      <li> as Object arrays (parameter `cast` = true)</li>
     *      <li> as a Map&lt;String,Object&gt; where key is the header name, and val is the data converted to
     *          Java objects (parameter `keyed` = true)</li>
     *      <li> or in an "extended" form (parameter `extended` = true) that returns an Object array where the first
     *      entry is the row number, the second is a String array holding the headers, and the third is an Object
     *      array holding the row data converted to Java objects.</li>
     *      <li> Resolving references to other data sources (parameter `relations` = true)</li>
     *  </ul>
     *
     * The following rules apply:
     * <ul>
     *   <li>if no Schema is present, rows will always return string arrays, not objects, as if `cast` was always off</li>
     *   <li>if `extended` is true, then `cast` is also true, but `keyed` is false</li>
     *   <li>if `keyed` is true, then `cast` is also true, but `extended` is false</li>
     * </ul>
     *
     * If a Schema is set on a table, the Field definitions will be used for parsing
     * data values to objects.
     *
     * @return Interator returning table rows as Objects, either Arrays or Maps
     */
    public Iterator<Object> iterator(boolean keyed, boolean extended, boolean cast, boolean relations){
       return new TableIterator<>(this, keyed, extended, cast, relations);
    }

    /**
     * This method creates an Iterator that will return table rows as String arrays.
     * It therefore disregards the Schema set on the table. It does not follow relations.
     *
     * @return Iterator that returns rows as string arrays.
     */
    public Iterator<String[]> stringArrayIterator() {
        return new SimpleTableIterator(this, false);
    }

    /**
     * This method creates an Iterator that will return table rows as String arrays.
     * It therefore disregards the Schema set on the table. It can be configured to follow relations.
     *
     * @param relations Whether references to other data sources get resolved
     * @return Iterator that returns rows as string arrays.
     */
    public Iterator<String[]> stringArrayIterator(boolean relations) {
        return new SimpleTableIterator(this, relations);
    }

    /**
     * This method creates an Iterator that will return table rows as a Map&lt;String,Object&gt;
     * where key is the header name, and val is the data converted to Java objects. It does not follow relations.
     *
     * @return Iterator that returns rows as Maps.
     */
    public Iterator<Map<String, Object>> mappingIterator() {
        return new TableIterator<>(this, true, false, true, false);
    }

    /**
     * This method creates an Iterator that will return table rows as a Map&lt;String,Object&gt;
     * where key is the header name, and val is the data converted to Java objects.
     * It can be configured to follow relations
     *
     * @param relations Whether references to other data sources get resolved
     * @return Iterator that returns rows as Maps.
     */
    public Iterator<Map<String, Object>> mappingIterator(boolean extended, boolean cast, boolean relations){
        return new TableIterator<>(this, true, extended, cast, relations);
    }

    public Map<Integer, Integer> getSchemaHeaderMapping() {
        if (null == schema) {
            return TableSchemaUtil
                    .createSchemaHeaderMapping(dataSource.getHeaders(), dataSource.getHeaders(), true);
        } else {
            return TableSchemaUtil
                    .createSchemaHeaderMapping(dataSource.getHeaders(), schema.getHeaders(), dataSource.hasReliableHeaders());
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
     */
    public String[] getHeaders(){
        if (null != schema) {
            return schema.getHeaders();
        }
        return this.dataSource.getHeaders();
    }

    /**
     * Read all data from the Table, each row as Object arrays if `cast` is set to true, String arrays if false.
     * This can be used for smaller data tables but for huge or unknown sizes, reading via iterator  is preferred,
     * as this method loads all data into RAM.
     *
     * It ignores relations to other data sources.
     *
     * The method is roughly implemented after
     * https://github.com/frictionlessdata/tableschema-py/blob/master/tableschema/table.py
     *
     * @return A list of table rows.
     */
    public List<Object[]> read(boolean cast){
        if(cast && (null == schema)){
            throw new TableSchemaException("Cannot cast without a schema");
        }
        if(cast && this.schema.isEmpty()){
            throw new InvalidCastException("Schema has no fields");
        }
        
        List<Object[]> rows = new ArrayList<>();
        try {
            Iterator<Object> iter = this.iterator(false, false, cast, false);
            while (iter.hasNext()) {
                Object row = iter.next();
                rows.add((Object[]) row);
            }
        } catch (InvalidCastException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return rows;
    }

    /**
     * Read all data from the Table, each row as Object arrays if a Schema is set on the table, String arrays if not.
     * This can be used for smaller data tables but for huge or unknown sizes, reading via iterator  is preferred,
     * as this method loads all data into RAM.
     *
     * It ignores relations to other data sources.
     *
     * The method is roughly implemented after
     * https://github.com/frictionlessdata/tableschema-py/blob/master/tableschema/table.py
     *
     * @return A list of table rows.
     */
    public List<Object[]> read(){
        boolean cast = (null != schema);
        return read(cast);
    }

    /**
     * Read all data from the Table and return it as JSON. If no Schema is set on the table, one will be inferred.
     * This can be used for smaller data tables but for huge or unknown sizes, there will be performance considerations,
     * as this method loads all data into RAM *and* does a costly schema inferal.
     *
     * It ignores relations to other data sources.
     *
     * @return A JSON representation of the data as a String.
     */
    public String asJson() {
        List<Map<String, Object>> rows = new ArrayList<>();
        Schema schema = (null != this.schema) ? this.schema : this.inferSchema();

        Iterator<Object> iter = this.iterator(false, false, true, false);
        iter.forEachRemaining((rec) -> {
            Object[] row = (Object[])rec;
            Map<String, Object> obj = new LinkedHashMap<>();
            int i = 0;
            for (Field field : schema.getFields()) {
                Object s = row[i];
                obj.put(field.getName(), field.formatValueForJson(s));
                i++;
            }
            rows.add(obj);
        });

        String retVal = null;
        ObjectMapper mapper = JsonUtil.getInstance().getMapper();
        try {
            retVal = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows);
        } catch (JsonProcessingException ex) {
            throw new JsonSerializingException(ex);
        }
        return retVal;
    }

    /**
     * Read all data from the Table and return it as a RFC 4180 compliant CSV string.
     * Column order will be deducted from the table data source.
     *
     * @return A CSV representation of the data as a String.
     */
    public String asCsv() {
        return asCsv(null, null);
    }

    /**
     * Return the data as a CSV string,
     *
     * - the `format` parameter decides on the CSV options. If it is null, then the file will
     *    be written as RFC 4180 compliant CSV
     * - the `headerNames` parameter decides on the order of the headers in the CSV file. If it is null,
     *    the order of the columns will be the same as in the data source.
     *
     * It ignores relations to other data sources.
     *
     * @param format the CSV format to use
     * @param headerNames the header row names in the order in which data should be exported
     *
     * @return A CSV representation of the data as a String.
     */
    public String asCsv(CSVFormat format, String[] headerNames) {
        StringBuilder out = new StringBuilder();
        try {
            if (null == headerNames) {
                return asCsv(format, getHeaders());
            }
            CSVFormat locFormat = (null != format)
                    ? format
                    : TableDataSource.getDefaultCsvFormat();

            locFormat = locFormat.builder().setHeader(headerNames).get();
            CSVPrinter csvPrinter = new CSVPrinter(out, locFormat);

            String[] headers = getHeaders();
            Map<Integer, Integer> mapping
                    = TableSchemaUtil.createSchemaHeaderMapping(headers, headerNames, dataSource.hasReliableHeaders());
            if ((null != schema)) {
                writeCSVData(mapping, schema, csvPrinter);
            } else {
                writeCSVData(mapping, csvPrinter);
            }
            csvPrinter.close();
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }
        String result = out.toString();
        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }
        if (result.endsWith("\r")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * Write Table data to a provided {@link java.io.Writer} - the `dataFormat` parameter decides on the data format,
     * either CSV or JSON.
     * @param out the Writer to write to
     * @param dataFormat the format to use, either CSV or JSON.
     */
    public void write(Writer out, TableDataSource.Format dataFormat) {
        try  {
            if (dataFormat.equals(TableDataSource.Format.FORMAT_CSV)) {
                try {
                    String[] headers;
                    if (null != schema) {
                        List<String> fieldNames = schema.getFields().stream().map(Field::getName).toList();
                        headers = fieldNames.toArray(new String[0]);
                    } else {
                        headers = dataSource.getHeaders();
                    }
                    writeCsv(out, this.format, headers);
                } catch (Exception ex) {
                    if (ex instanceof RuntimeException)
                        throw ex;
                    throw new RuntimeException(ex);
                }
            } else if (dataFormat.equals(TableDataSource.Format.FORMAT_JSON)) {
                String content = this.asJson();
                out.write(content);
            }
        } catch (IOException ex) {
            throw new TableIOException(ex);
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
        write(out, TableDataSource.Format.FORMAT_CSV);
        this.format = oldFormat;
    }

    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is null, then the data will
     * be written in the CSV defined by the CSV format of the Table
     *
     * @param outputFile the File to write to
     * @param format the CSV format to use
     */
    public void writeCsv(File outputFile, CSVFormat format){
        try (FileWriter fw = new FileWriter(outputFile)) {
            writeCsv(fw, format);
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }
    }

    /**
     * Validates that names of the headers are as declared in the Schema, and
     * throws a TableValidationException if they aren't. If the headers derived from the
     * TableDataSource aren't reliable (eg. JSON array of JSON objects where properties
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
        String[] headers = dataSource.getHeaders();
        // if the data has no headers (CSV table without header row), we can't validate against the Schema
        if (null == headers) {
            return;
        }
        List<String> declaredHeaders = schema.getFields().stream().map(Field::getName).toList();
        List<String> foundHeaders = Arrays.asList(headers);
        //If we have JSON data, fields with `null` values might be omitted, therefore do not do a strict check
        if (dataSource.hasReliableHeaders()) {
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
        if (null != schema)
            schema.validate(this);
    }

    /**
     * The type inferring algorithm takes a data sample and tries to cast each row to
     * the {@link Field} types and each successful type casting increments a popularity score
     * for the Field in question. At the end, the best score so far is returned.
     *
     * This method iterates through the whole data set, which can be very costly for huge
     * CSV/JSON files
     *
     * For {@link BeanSchema}, the operation is much less costly, it is simply done via reflection
     * on the Bean class.
     *
     * @return the created Schema
     *
     */
    public Schema inferSchema() throws TypeInferringException{
        return inferSchema(-1);
    }

    /**
     * The type inferring algorithm takes a data sample and tries to cast each row to
     * the {@link Field} types and each successful type casting increments a popularity score
     * for the Field in question. At the end, the best score so far is returned.
     *
     * This method iterates through a limited row number
     *
     * For {@link BeanSchema}, the operation is simply done via reflection
     * on the Bean class, so the `rowLimit`does not have any effect.
     *
     * @return the created Schema
     *
     */
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
            return Schema.infer(data, headers, rowLimit);

        } catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    public Table setCsvFormat(CSVFormat format) {
        this.format = format;
        if ((null != dataSource) && (dataSource instanceof CsvTableDataSource)) {
            ((CsvTableDataSource) dataSource).setFormat(format);
        }
        return this;
    }

    public CSVFormat getCsvFormat() {
        return format;
    }

    /**
     * Get the current Schema for this Table or `null` if no Schema is set.
     *
     * @return the active Schema
     */
    public Schema getSchema(){
        return this.schema;
    }

    /**
     * Set a Schema for this Table. If the Table is connected to a TableDataSource, ie. holds data,
     * then the data will be validated against the new Schema.
     * @param schema the Schema to set
     */
    public Table setSchema(Schema schema) {
        this.schema = schema;
        if (null != dataSource)
            validate();
        return this;
    }

    /**
     * Get the current TableDataSource for this Table
     * @return the active TableDataSource
     */
    public TableDataSource getTableDataSource(){
        return this.dataSource;
    }

    /**
     * Set a TableDataSource for this Table, ie. set the data for the Table. If this Table has an active Schema,
     * then the data will be validated against the Schema.
     * @param fmt the TableDataSource to set
     */
    public Table setTableDataSource(TableDataSource fmt) {
        this.dataSource = fmt;
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
            Iterator<Object> iterator = this.iterator(false, false, false, true);
            Iterator<Object> oIter = ((Table) o).iterator(false, false, false, true);
            while (iterator.hasNext()) {
                Object[] arr = (Object[]) iterator.next();
                Object[] oArr = (Object[]) oIter.next();
                equals = equals & Arrays.equals(arr, oArr);
            }
            return equals;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSource, schema, format);
    }



    /**
     * Write as CSV file, the `format` parameter decides on the CSV options. If it is
     * null, then the file will be written as RFC 4180 compliant CSV
     * @param out the Writer to write to
     * @param format the CSV format to use
     * @param sortedHeaders the header row names in the order in which data should be
     *                      exported
     */
    private void writeCsv(Writer out, CSVFormat format, String[] sortedHeaders) {
        try {
            if (null == sortedHeaders) {
                writeCsv(out, format, getHeaders());
                return;
            }
            CSVFormat locFormat = (null != format)
                    ? format
                    : TableDataSource.getDefaultCsvFormat();

            locFormat = locFormat.builder().setHeader(sortedHeaders).get();
            CSVPrinter csvPrinter = new CSVPrinter(out, locFormat);

            String[] headers = getHeaders();
            Map<Integer, Integer> mapping
                    = TableSchemaUtil.createSchemaHeaderMapping(headers, sortedHeaders, dataSource.hasReliableHeaders());
            if ((null != schema)) {
                writeCSVData(mapping, schema, csvPrinter);
            } else {
                writeCSVData(mapping, csvPrinter);
            }
            csvPrinter.close();
        } catch (IOException ex) {
            throw new TableIOException(ex);
        }
    }


    /**
     * Append the data to a {@link org.apache.commons.csv.CSVPrinter}. Column sorting is according to the mapping
     * @param mapping the mapping of the column numbers in the CSV file to the column numbers in the data source
     * @param schema the Schema to use for formatting the data
     * @param csvPrinter the CSVPrinter to write to
     */
    private void writeCSVData(Map<Integer, Integer> mapping, Schema schema, CSVPrinter csvPrinter) {
        Iterator<Object> iter = this.iterator(false, false, true, false);
        iter.forEachRemaining((rec) -> {
            Object[] row = (Object[])rec;
            Object[] sortedRec = new Object[row.length];
            for (int i = 0; i < row.length; i++) {
                sortedRec[mapping.get(i)] = row[i];
            }
            List<String> obj = new ArrayList<>();
            int i = 0;
            for (Field field : schema.getFields()) {
                Object s = sortedRec[i];
                obj.add(field.formatValueAsString(s));
                i++;
            }

            try {
                csvPrinter.printRecord(obj);
            } catch (Exception ex) {
                throw new TableIOException(ex);
            }
        });
    }

    private void writeCSVData(Map<Integer, Integer> mapping, CSVPrinter csvPrinter) {
        Iterator<Object> iter = this.iterator(false, false, false, false);
        iter.forEachRemaining((rec) -> {
            Object[] row = (Object[])rec;
            Object[] sortedRec = new Object[row.length];
            for (int i = 0; i < row.length; i++) {
                sortedRec[mapping.get(i)] = row[i];
            }
            List<String> obj = new ArrayList<>();

            for (int j = 0; j < sortedRec.length; j++) {
                Object s = sortedRec[j];
                obj.add((null != s) ? s.toString() : "");
            }

            try {
                csvPrinter.printRecord(obj);
            } catch (Exception ex) {
                throw new TableIOException(ex);
            }
        });
    }
}