package io.frictionlessdata.tableschema.io;

import java.io.*;

public class LocalFilesReference implements FileReference<File> {
    File inputFile;
    FileInputStream is;

    public LocalFilesReference(File source) {
        inputFile = source;
    }

    @Override
    public InputStream getInputStream() throws Exception {
        if (null == is)
            is = new FileInputStream(inputFile);
        return is;
    }

    @Override
    public String getLocator() {
        return inputFile.getAbsolutePath();
    }

    @Override
    public String getFileName(){
        return inputFile.getName();
    }

    public void close() throws IOException {
        is.close();
    }
}
