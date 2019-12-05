package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exception.TableSchemaException;
import io.frictionlessdata.tableschema.exception.TypeInferringException;
import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import java.io.File;

import io.frictionlessdata.tableschema.iterator.TableIterator;
import org.apache.commons.csv.CSVFormat;
import org.json.*;

import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.*;

/**
 * This class represents a CSV table
 * 
 */
public class Table{
    private DataSource dataSource = null;
    private Schema schema = null;
    private CSVFormat format;
    private Map<String, Object> fieldOptions;

    /**
     * Constructor using either a CSV or JSON array-containing string.
     * @param dataSource the CSV or JSON content for the Table
     */
    public Table(String dataSource) {
        this.dataSource = DataSource.createDataSource(dataSource);
    }

    /**
     * Constructor using an {@link java.io.InputStream} for reading both the CSV
     * data and the table schema.
     * @param dataSource InputStream for reading the CSV from
     * @param schema InputStream for reading table schema from
     * @throws Exception if either reading or parsing throws an Exception
     */
    public Table(InputStream dataSource, InputStream schema) throws Exception{
        this.dataSource = DataSource.createDataSource(dataSource);
        this.schema = new Schema(schema, true);
    }

    public Table(File dataSource, File basePath) throws Exception{
        this.dataSource = new CsvDataSource(dataSource, basePath);
    }

    public Table(File dataSource, File basePath, Schema schema) throws Exception{
        this.dataSource = new CsvDataSource(dataSource, basePath);
        this.schema = schema;
    }

    public Table(String dataSource, Schema schema) {
        this.dataSource = DataSource.createDataSource(dataSource);
        this.schema = schema;
    }

    public Table(URL dataSource) {
        this.dataSource = new CsvDataSource(dataSource);
    }

    public Table(URL dataSource, Schema schema) {
        this.dataSource = new CsvDataSource(dataSource);
        this.schema = schema;
    }
    
    public Table(URL dataSource, URL schema) throws Exception{
        this(dataSource, new Schema(schema, true));
    }

    public Iterator<Object[]> iterator() throws Exception{
       return new TableIterator<Object[]>(this);
    }

    public Iterator<Object[]> iterator(boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception{
       return new TableIterator<Object[]>(this, keyed, extended, cast, relations);
    }

    public Iterator<Map> keyedIterator() throws Exception{
        return new TableIterator<Map>(this);
    }

    public Iterator<Map> keyedIterator(boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception{
        return new TableIterator<Map>(this, keyed, extended, cast, relations);
    }
    
    public String[] getHeaders() throws Exception{
        return this.dataSource.getHeaders();
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
        this.dataSource.writeCsv(out, format);
    }

    public void writeCsv(File outputFile, CSVFormat format) throws Exception{
        this.dataSource.writeCsv(outputFile, format);
    }
    
    public Schema inferSchema() throws TypeInferringException{
        return inferSchema(-1);
    }
    
    public Schema inferSchema(int rowLimit) throws TypeInferringException{
        try{
            JSONObject schemaJson = TypeInferrer.getInstance().infer(read(), getHeaders(), rowLimit);
            schema = new Schema(schemaJson.toString(), false);
            return schema;
            
        }catch(Exception e){
            throw new TypeInferringException(e);
        }
    }

    public Table setCsvFormat(CSVFormat format) {
        this.format = format;
        if ((null != dataSource) && (dataSource instanceof CsvDataSource)) {
            ((CsvDataSource)dataSource).format(format);
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
    
    public DataSource dataSource(){
        return this.dataSource;
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
        return Objects.hash(dataSource, schema, format);
    }

}