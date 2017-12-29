package io.frictionlessdata.tableschema;

import com.opencsv.CSVWriter;
import io.frictionlessdata.tableschema.exceptions.TypeInferringException;
import io.frictionlessdata.tableschema.datasources.CsvDataSource;
import io.frictionlessdata.tableschema.datasources.DataSource;
import io.frictionlessdata.tableschema.exceptions.InvalidCastException;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import org.json.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * 
 */
public class Table{
    private Object dataSource = null;
    private Schema schema = null;
    
    private CSVParser parser = null;
    
    public Table(String dataSourceFilename) throws Exception{
        this.dataSource = dataSourceFilename;
        initParser();
    }
    
    public Table(String dataSourceFilename, JSONObject schemaJson) throws Exception{
        this.dataSource = dataSourceFilename;
        this.schema = new Schema(schemaJson);
        initParser();
    }
    
    public Table(String dataSourceFilename, Schema schema) throws Exception{
        this.dataSource = dataSourceFilename;
        this.schema = schema;
        initParser();
    }
    
    public Table(URL url) throws Exception{
        this.dataSource = url;
        initParser();
    }
    
    public Table(URL url, JSONObject schemaJson) throws Exception{
        this.dataSource = url;
        this.schema = new Schema(schemaJson);
        initParser();
    }
    
    public Table(URL url, Schema schema) throws Exception{
        this.dataSource = url;
        this.schema = schema;
        initParser();
    }
    
    public Table(URL dataSourceUrl, URL schemaUrl) throws Exception{
        this.dataSource = dataSourceUrl;
        this.schema = new Schema(schemaUrl);
        initParser();
    }
    
    private void initParser() throws Exception{
        
        if(this.getDataSource() instanceof URL){
            this.parser = CSVParser.parse((URL)this.getDataSource(), Charset.forName("UTF-8"), CSVFormat.RFC4180.withHeader()); 
        
        }else if(this.getDataSource() instanceof String){
            // If it's not a URL String, then it's path to a CSV file.
            String path = (String)this.getDataSource();
            
            // The path value can either be a relative path or a full path.
            // If it's a relative path then build the full path by using the working directory.
            File f = new File(path);
            if(!f.exists()) { 
                path = System.getProperty("user.dir") + "/" + path;
            }

            // Read the file.
            Reader fr = new FileReader(path);
            
            // Get the parser.
            this.parser = CSVFormat.RFC4180.withHeader().parse(fr);
            
        }else{
            throw new Exception("Invalid data source type. Must either be a URL or a String file path.");
        }
    }
    
    public Iterator<CSVRecord> iterator() throws IOException, Exception{
        // Return iterator.
        Iterator<CSVRecord> iter = this.parser.iterator();
        return iter; 
    }
    
    /*s
    public TableIterator iterator(boolean keyed){
       return new TableIterator(this, keyed);
    }
    
    public TableIterator iterator(boolean keyed, boolean extended){
       return new TableIterator(this, keyed, extended);
    }
    
    public TableIterator iterator(boolean keyed, boolean extended, boolean cast){
       return new TableIterator(this, keyed, extended, cast);
    }
    
    public TableIterator iterator(boolean keyed, boolean extended, boolean cast, boolean relations){
       return new TableIterator(this, keyed, extended, cast, relations);
    }**/
    
    
    /**
     * 
     * @return 
     */
    public String[] getHeaders(){
        // Get a copy of the header map that iterates in column order.
        // The map keys are column names. The map values are 0-based indices.
        Map<String, Integer> headerMap = this.parser.getHeaderMap();
        
        // Generate list of keys
        List<String> headerVals = new ArrayList();
        
        Iterator it = headerMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            headerVals.add((String)pair.getKey());
        }
        
        // Return string array of keys.
        return headerVals.toArray(new String[0]);
    }
    
    /**
     * 
     * @param outputFilePath
     * @throws Exception 
     */
    public void save(String outputFilePath) throws Exception{            
       try(Writer out = new BufferedWriter(new FileWriter(outputFilePath))) {
            CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.RFC4180);
            
            if(this.getHeaders() != null){
                csvPrinter.printRecord(this.getHeaders());
            }
            
            Iterator<CSVRecord> recordIter = this.iterator();
            while(recordIter.hasNext()){
                CSVRecord record = recordIter.next();
                csvPrinter.printRecord(record);
            }
            
            csvPrinter.flush();
            csvPrinter.close();
                
       }catch(Exception e){
            throw e;
       }
  
       /**
       Iterator<CSVRecord> recordIter = this.iterator();
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath))) {
            if(parser.getHeaderMap() != null){
                //writer.writeNext();
            }
            while(recordIter.hasNext()){
                CSVRecord record = recordIter.next();
                
                Iterator<String> colIter = record.iterator();
                
                List<String> colList = new ArrayList();
                while(colIter.hasNext()){
                    colList.add(colIter.next());
                }
                
                writer.writeNext(colList.toArray(new String[0]));
                
            }
            
            writer.flush();
        }**/
    }
    /**
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
    }**/
    
    /**
     * 
     * @return
     * @throws Exception 
     */
    public List<CSVRecord> read() throws Exception{
        return this.parser.getRecords();
    }
    
    /**
    public Schema inferSchema() throws TypeInferringException{
        try{
            JSONObject schemaJson = TypeInferrer.getInstance().infer(this.read(), this.getHeaders());
            this.schema = new Schema(schemaJson);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }**/
    
    /**
    public Schema inferSchema(int rowLimit) throws TypeInferringException{
        try{
            JSONObject schemaJson = TypeInferrer.getInstance().infer(this.read(), this.getHeaders(), rowLimit);
            this.schema = new Schema(schemaJson);
            return this.schema;
            
        }catch(Exception e){
            throw new TypeInferringException();
        }
    }**/
    
    public Schema getSchema(){
        return this.schema;
    }
    
    public Object getDataSource(){
        return this.dataSource;
    }
}
