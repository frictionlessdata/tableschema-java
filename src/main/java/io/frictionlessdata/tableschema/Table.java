package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.io.File;

import org.apache.commons.csv.CSVFormat;
import org.json.*;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a CSV table
 * 
 */
public class Table{
    private DataSource dataSource = null;
    private Schema schema = null;
    private CSVFormat format;

    /**
     * Constructor using an {@link java.io.InputStream} for reading both the CSV
     * data and the table schema.
     * @param dataSource InputStream for reading the CSV from
     * @param schema InputStream for reading table schema from
     * @throws Exception if either reading or parsing throws an Exception
     */
    public Table(InputStream dataSource, InputStream schema) throws Exception{
        this.dataSource = DataSource.createDataSource(dataSource, null);
        this.schema = new Schema(schema, true);
    }

    public Table(File dataSource, File basePath) throws Exception{
        this.dataSource = new CsvDataSource(dataSource, basePath);
    }
    
    public Table(File dataSource, File basePath, JSONObject schema) throws Exception{
        this.dataSource = new CsvDataSource(dataSource, basePath);
        if (null != schema)
            this.schema = new Schema(schema.toString(), false);
    }
    
    public Table(File dataSource, File basePath, Schema schema) throws Exception{
        this.dataSource = new CsvDataSource(dataSource, basePath);
        this.schema = schema;
    }
    
    public Table(String dataSource) {
        this.dataSource = DataSource.createDataSource(dataSource, null);
    }

    public Table(String dataSource, Schema schema) {
        this.dataSource = DataSource.createDataSource(dataSource, null);
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
        this.dataSource = new CsvDataSource(dataSource);
        this.schema = new Schema(schema, false);
    }

    public Iterator iterator() throws Exception{
       return new TableIterator(this);
    }
    
    public Iterator iterator(boolean keyed) throws Exception{
       return new TableIterator(this, keyed);
    }
    
    public Iterator iterator(boolean keyed, boolean extended) throws Exception{
       return new TableIterator(this, keyed, extended);
    }
    
    public Iterator iterator(boolean keyed, boolean extended, boolean cast) throws Exception{
       return new TableIterator(this, keyed, extended, cast);
    }
    
    public Iterator iterator(boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception{
       return new TableIterator(this, keyed, extended, cast, relations);
    }
    
    public String[] getHeaders() throws Exception{
        return this.dataSource.getHeaders();
    }

    public void save(File outputFile) throws Exception{
       this.dataSource.writeCsv(outputFile);
    }
    public List<Object[]> read(boolean cast) throws Exception{
        if(cast && !this.schema.hasFields()){
            throw new InvalidCastException();
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
        return this.read(false);
    }
    
    public Schema inferSchema() throws TypeInferringException{
        try{
            JSONObject schemaJson = TypeInferrer.getInstance().infer(this.read(), this.getHeaders());
            this.schema = new Schema(schemaJson.toString(), false);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    
    public Schema inferSchema(int rowLimit) throws TypeInferringException{
        try{
            JSONObject schemaJson = TypeInferrer.getInstance().infer(this.read(), this.getHeaders(), rowLimit);
            this.schema = new Schema(schemaJson.toString(), false);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }

    public Table csvFormat(CSVFormat format) {
        this.format = format;
        return this;
    }

    public CSVFormat format() {
        return format;
    }

    public Schema schema(){
        return this.schema;
    }
    
    public DataSource dataSource(){
        return this.dataSource;
    }
}