package io.frictionlessdata.tableschema.datasourceformat;

import com.google.common.collect.Iterators;
import io.frictionlessdata.tableschema.util.TableSchemaUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * 
 */
public abstract class AbstractDataSourceFormat implements DataSourceFormat {
    String[] headers;
    Object dataSource = null;
    private File workDir;

    AbstractDataSourceFormat(){}

    AbstractDataSourceFormat(URL dataSource){
        this.dataSource = dataSource;
    }

    AbstractDataSourceFormat(File dataSource, File workDir){
        this.dataSource = dataSource;
        this.workDir = workDir;
    }

    AbstractDataSourceFormat(String dataSource){
        this.dataSource = dataSource;
    }


    @Override
    public List<String[]> data() throws Exception{
        List<String[]> data = new ArrayList<>();
        iterator().forEachRemaining(data::add);
        return data;
    }


    String getFileContents(String path) throws IOException {
        return DataSourceFormat.getFileContents(path, workDir);
    }

}
