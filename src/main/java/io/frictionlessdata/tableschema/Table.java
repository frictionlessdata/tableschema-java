package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.datasourceformats.CsvDataSourceFormat;
import io.frictionlessdata.tableschema.datasourceformats.DataSourceFormat;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import java.io.File;

import io.frictionlessdata.tableschema.iterator.SimpleTableIterator;
import io.frictionlessdata.tableschema.iterator.TableIterator;
import org.apache.commons.csv.CSVFormat;

import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.*;

/**
 * This class represents a CSV table
 * 
 */
public class Table{
    private DataSourceFormat dataSourceFormat = null;
    private Schema schema = null;
    private CSVFormat format;
    private Map<String, Object> fieldOptions;

    /**
     * Constructor using either a CSV or JSON array-containing string.
     * @param dataSource the CSV or JSON content for the Table
     */
    public Table(String dataSource) {
        this.dataSourceFormat = DataSourceFormat.createDataSource(dataSource);
    }

    /**
     * Constructor using an {@link java.io.InputStream} for reading both the CSV
     * data and the table schema.
     * @param dataSource InputStream for reading the CSV from
     * @param schema InputStream for reading table schema from
     * @throws Exception if either reading or parsing throws an Exception
     */
    public Table(InputStream dataSource, InputStream schema) throws Exception{
        this.dataSourceFormat = DataSourceFormat.createDataSource(dataSource);
        this.schema = new Schema(schema, true);
    }

    public Table(File dataSource, File basePath) throws Exception{
        this.dataSourceFormat = new CsvDataSourceFormat(dataSource, basePath);
    }

    public Table(File dataSource, File basePath, Schema schema) throws Exception{
        this.dataSourceFormat = new CsvDataSourceFormat(dataSource, basePath);
        this.schema = schema;
    }

    public Table(String dataSource, Schema schema) {
        this.dataSourceFormat = DataSourceFormat.createDataSource(dataSource);
        this.schema = schema;
    }

    public Table(URL dataSource) {
        this.dataSourceFormat = new CsvDataSourceFormat(dataSource);
    }

    public Table(URL dataSource, Schema schema) {
        this.dataSourceFormat = new CsvDataSourceFormat(dataSource);
        this.schema = schema;
    }
    
    public Table(URL dataSource, URL schema) throws Exception{
        this(dataSource, new Schema(schema, true));
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
    
    public String[] getHeaders() throws Exception{
        return this.dataSourceFormat.getHeaders();
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
        this.dataSourceFormat.writeCsv(out, format);
    }

    public void writeCsv(File outputFile, CSVFormat format) throws Exception{
        this.dataSourceFormat.writeCsv(outputFile, format);
    }
    
    public Schema inferSchema() throws TypeInferringException{
        return inferSchema(-1);
    }
    
    public Schema inferSchema(int rowLimit) throws TypeInferringException{
        try{
            String schemaJson = TypeInferrer.getInstance().infer(read(), getHeaders(), rowLimit);
            schema = new Schema(schemaJson, false);
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