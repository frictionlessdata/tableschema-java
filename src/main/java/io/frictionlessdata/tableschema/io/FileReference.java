package io.frictionlessdata.tableschema.io;

import java.io.IOException;
import java.io.InputStream;

public interface FileReference<T> {

    InputStream getInputStream() throws Exception;

    String getLocator();

    String getFileName();

    void close() throws IOException;
}
