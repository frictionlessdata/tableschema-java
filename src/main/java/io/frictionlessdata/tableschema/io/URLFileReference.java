package io.frictionlessdata.tableschema.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class URLFileReference implements FileReference<URL> {
    URL inputFile;
    InputStream is;

    public URLFileReference(URL source) {
        inputFile = source;
    }

    @Override
    public InputStream getInputStream() throws Exception {
        if (null == is)
            is = inputFile.openStream();
        return is;
    }

    @Override
    public String getLocator() {
        return inputFile.toExternalForm();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
