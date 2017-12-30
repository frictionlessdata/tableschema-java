package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import org.json.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class Table{
    private DataSource dataSource = null;
    private Schema schema = null;
    
    public Table(String dataSourceFilename) throws Exception{
        this.dataSource = new CsvDataSource(dataSourceFilename);
    }
    
    public Table(String dataSourceFilename, JSONObject schemaJson) throws Exception{
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.schema = new Schema(schemaJson);
    }
    
    public Table(String dataSourceFilename, Schema schema) throws Exception{
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.schema = schema;
    }
    
    public Table(URL url) throws Exception{
        this.dataSource = new CsvDataSource(url);
    }
    
    public Table(URL url, JSONObject schemaJson) throws Exception{
        this.dataSource = new CsvDataSource(url);
        this.schema = new Schema(schemaJson);
    }
    
    public Table(URL url, Schema schema) throws Exception{
        this.dataSource = new CsvDataSource(url);
        this.schema = schema;
    }
    
    public Table(URL dataSourceUrl, URL schemaUrl) throws Exception{
        this.dataSource = new CsvDataSource(dataSourceUrl);
        this.schema = new Schema(schemaUrl);
    }
    
    public TableIterator iterator() throws Exception{
       return new TableIterator(this);
    }
    
    public TableIterator iterator(boolean keyed) throws Exception{
       return new TableIterator(this, keyed);
    }
    
    public TableIterator iterator(boolean keyed, boolean extended) throws Exception{
       return new TableIterator(this, keyed, extended);
    }
    
    public TableIterator iterator(boolean keyed, boolean extended, boolean cast) throws Exception{
       return new TableIterator(this, keyed, extended, cast);
    }
    
    public TableIterator iterator(boolean keyed, boolean extended, boolean cast, boolean relations) throws Exception{
       return new TableIterator(this, keyed, extended, cast, relations);
    }
    
    public String[] getHeaders() throws Exception{
        return this.dataSource.getHeaders();
    }
    
    public void save(String outputFilePath) throws Exception{
       this.dataSource.write(outputFilePath);
    }
    public List<Object[]> read(boolean cast) throws Exception{
        if(cast && !this.schema.hasFields()){
            throw new InvalidCastException();
        }
        
        List<Object[]> rows = new ArrayList();
        
        TableIterator<Object[]> iter = this.iterator(false, false, cast, false);
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
            this.schema = new Schema(schemaJson);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    
    public Schema inferSchema(int rowLimit) throws TypeInferringException{
        try{
            JSONObject schemaJson = TypeInferrer.getInstance().infer(this.read(), this.getHeaders(), rowLimit);
            this.schema = new Schema(schemaJson);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    
    public Schema getSchema(){
        return this.schema;
    }
    
    public DataSource getDataSource(){
        return this.dataSource;
    }
}