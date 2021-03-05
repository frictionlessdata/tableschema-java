package io.frictionlessdata.tableschema.datasourceformat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
