package dk.magenta.datafordeler.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lars on 08-06-17.
 */
public class CloseDetectInputStream extends InputStream {

    private InputStream in;
    private List<Runnable> beforeCloseListeners = new ArrayList<>();
    private List<Runnable> afterCloseListeners = new ArrayList<>();

    public CloseDetectInputStream(InputStream in) {
        if (in == null) {
            throw new IllegalArgumentException("Wrapped stream must be non-null");
        }
        this.in = in;
    }

    public void addBeforeCloseListener(Runnable closeListener) {
        this.beforeCloseListeners.add(closeListener);
    }

    public void addAfterCloseListener(Runnable closeListener) {
        this.afterCloseListeners.add(closeListener);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return this.in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public void close() throws IOException {
        this.runListeners(this.beforeCloseListeners);
        this.in.close();
        this.runListeners(this.afterCloseListeners);
    }

    private void runListeners(List<Runnable> listeners) {
        for (Runnable closeListener : listeners) {
            try {
                closeListener.run();
            } catch (Exception e) {}
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
    }

    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }

    @Override
    public int read() throws IOException {
        return this.in.read();
    }
}
