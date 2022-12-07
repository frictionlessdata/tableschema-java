package io.frictionlessdata.tableschema.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements the FileOrUrl logic of the tableschema standard where a reference can either point to the
 * local file system or a web URL.
 * @see LocalFileReference
 * @see URLFileReference
 * @param <T>
 */
public interface FileReference<T> {

    InputStream getInputStream() throws IOException;

    String getLocator();

    String getFileName();

    void close() throws IOException;
}
