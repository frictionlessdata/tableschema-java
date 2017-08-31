package io.frictionlessdata.tableschema;

import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import org.json.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 *
 * 
 */
public class Table{
    private DataSource dataSource = null;
    private Schema schema = null;
    private TypeInferrer typeInferrer = null;
    
    public Table(String dataSourceFilename) throws Exception{
        // FIXME: Don't assume it is always CSV.
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.typeInferrer = new TypeInferrer();
        // Infer schema?
    }
    
    public Table(String dataSourceFilename, JSONObject schemaJson) throws Exception{
        // FIXME: Don't assume it is always CSV.
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.typeInferrer = new TypeInferrer();
        this.schema = new Schema(schemaJson);
    }
    
    public Table(String dataSourceFilename, Schema schema) throws Exception{
        // FIXME: Don't assume it is always CSV.
        this.dataSource = new CsvDataSource(dataSourceFilename);
        this.typeInferrer = new TypeInferrer();
        this.schema = schema;
    }
    
    public Table(URL url) throws Exception{
        this.dataSource = new CsvDataSource(url);
        this.typeInferrer = new TypeInferrer();
        // Infer schema?
    }
    
    public Table(URL url, JSONObject schemaJson) throws Exception{
        this.dataSource = new CsvDataSource(url);
        this.typeInferrer = new TypeInferrer();
        // Infer schema?
        this.schema = new Schema(schemaJson);
    }
    
    public Table(URL url, Schema schema) throws Exception{
        this.dataSource = new CsvDataSource(url);
        this.typeInferrer = new TypeInferrer();
        // Infer schema?
        this.schema = schema;
    }
    
    public Iterator<Object[]> iterator(){
       return new TableIterator(this.schema, this.dataSource.iterator());
    }
    
    public String[] headers(){
        return this.dataSource.getHeaders();
    }
    
    public void write(String outputFilePath) throws Exception{
       this.dataSource.write(outputFilePath);
    }
    
    public List<String[]> read() throws Exception{
       return this.dataSource.data();
    }
    
    public Schema inferSchema() throws TypeInferringException{
        try{
            JSONObject schemaJson = this.typeInferrer.infer(this.read(), this.headers());
            this.schema = new Schema(schemaJson);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    
    public Schema inferSchema(int rowLimit) throws TypeInferringException{
        try{
            JSONObject schemaJson = this.typeInferrer.infer(this.read(), this.headers(), rowLimit);
            this.schema = new Schema(schemaJson);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }
    
    public Schema getSchema(){
        return this.schema;
    }
}
