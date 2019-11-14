package io.frictionlessdata.tableschema.datasources;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 
 */
public abstract class AbstractDataSource implements DataSource {
    Object dataSource = null;
    File workDir;

    public AbstractDataSource(InputStream inStream, File workDir) throws IOException{
        this.workDir = workDir;
        try (InputStreamReader ir = new InputStreamReader(inStream)) {
            try (BufferedReader rdr = new BufferedReader(ir)) {
                String dSource = rdr.lines().collect(Collectors.joining("\n"));
                this.dataSource = new JSONArray(dSource);
            }
        }
    }

    public AbstractDataSource(URL dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    public AbstractDataSource(File dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    public AbstractDataSource(String dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    @Override
    abstract public Iterator<String[]> iterator() throws Exception;
    
    @Override
    abstract public String[] getHeaders() throws Exception;
    
    @Override
    abstract public List<String[]> data() throws Exception;
    
    @Override
    abstract public void write(File outputFile) throws Exception;

    public void writeCsv(File outputFile) throws Exception {
        try (Writer out = new BufferedWriter(new FileWriter(outputFile));
             CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.RFC4180)) {

            String[] headers = getHeaders();
            if (headers != null) {
                csvPrinter.printRecord((Object[]) headers);
            }

            for (String[] record : data()) {
                csvPrinter.printRecord(record);
            }

            csvPrinter.flush();

        } catch (Exception e) {
            throw e;
        }
    }
}
