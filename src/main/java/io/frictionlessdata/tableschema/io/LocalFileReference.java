package io.frictionlessdata.tableschema.io;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LocalFileReference implements FileReference<File> {
    private File basePath;
    private File inputFile;
    private String relativePath;
    private boolean isInArchive;
    private FileInputStream is;


    public LocalFileReference(File inputFile) {
        this.basePath = inputFile.getParentFile();
        this.relativePath = inputFile.getName();
        this.inputFile = inputFile;
    }

    public LocalFileReference(File basePath, String relativePath) {
        this.basePath = basePath;
        this.relativePath = relativePath;
        inputFile = new File (basePath, relativePath);

        if (basePath.getName().toLowerCase().endsWith(".zip")) {
            isInArchive = true;
        }
    }

    @Override
    public InputStream getInputStream() throws Exception {
        if (this.isInArchive) {
            ZipFile zipFile = new ZipFile(basePath);
            ZipEntry entry = findZipEntry(zipFile, relativePath);
            if (null == entry) {
                throw new FileNotFoundException(basePath.toString()+File.separator+relativePath);
            }
            return zipFile.getInputStream(entry);
        } else {
            if (null == is)
                is = new FileInputStream(inputFile);
            return is;
        }
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
        if (null != is)
            is.close();
    }

    /**
     * Take a ZipFile and look for the `filename` entry. If it is not on the top-level,
     * look for directories and go into them (but only one level deep) and look again
     * for the `filename` entry
     * @param zipFile the ZipFile to use for looking for the `filename` entry
     * @param fileName name of the entry we are looking for
     * @return ZipEntry if found, null otherwise
     */
    private static ZipEntry findZipEntry(ZipFile zipFile, String fileName) {
        ZipEntry entry = zipFile.getEntry(fileName);
        if (null != entry)
            return entry;
        else {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory()) {
                    entry = zipFile.getEntry(zipEntry.getName()+fileName);
                    if (null != entry)
                        return entry;
                }
            }
        }
        return null;
    }
}
