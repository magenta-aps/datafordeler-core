package dk.magenta.datafordeler.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * InputStream that can execute callbacks when it is closed.
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

    /**
     * Add a callback to be run when the close() method is called, before the 
     * wrapped stream close() method is called
     */
    public void addBeforeCloseListener(Runnable closeListener) {
        this.beforeCloseListeners.add(closeListener);
    }

    /**
     * Add a callback to be run when the close() method is called, after the 
     * wrapped stream close() method is called
     */
    public void addAfterCloseListener(Runnable closeListener) {
        this.afterCloseListeners.add(closeListener);
    }

    /**
     * Wraps the read(byte[] b) method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#read--)
     */
    @Override
    public int read(byte[] b) throws IOException {
        return this.in.read(b);
    }

    /**
     * Wraps the read(byte[] b, int off, int len) method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#read-byte:A-int-int-)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.in.read(b, off, len);
    }

    /**
     * Wraps the skip(long n) method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#skip-long-)
     */
    @Override
    public long skip(long n) throws IOException {
        return this.in.skip(n);
    }

    /**
     * Wraps the available() method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#available--)
     */
    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    /**
     * Run registered callbacks before and after calling the wrapped stream close()
     */
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

    /**
     * Wraps the mark(int readlimit) method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#mark-int-)
     */
    @Override
    public synchronized void mark(int readlimit) {
        this.in.mark(readlimit);
    }

    /**
     * Wraps the reset() method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#reset--)
     */
    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
    }

    /**
     * Wraps the markSupported() method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#markSupported--)
     */
    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }

    /**
     * Wraps the read() method
     * (https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html#read--)
     */
    @Override
    public int read() throws IOException {
        return this.in.read();
    }
}
