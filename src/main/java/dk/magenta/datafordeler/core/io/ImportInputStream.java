package dk.magenta.datafordeler.core.io;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImportInputStream extends WrappedInputStream {

    public ImportInputStream(InputStream inner) {
        super(inner);
    }

    public ImportInputStream(InputStream inner, File cacheFile) {
        this(inner);
        this.cacheFiles.add(cacheFile);
    }

    public ImportInputStream(InputStream inner, List<File> cacheFiles) {
        this(inner);
        this.cacheFiles.addAll(cacheFiles);
    }


    private ArrayList<File> cacheFiles = new ArrayList<>();

    public void addCacheFile(File cacheFile) {
        this.cacheFiles.add(cacheFile);
    }

    public ArrayList<File> getCacheFiles() {
        return this.cacheFiles;
    }
}
