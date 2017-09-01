package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.exception.DataFordelerException;

/**
 * Created by lars on 29-05-17.
 */
public abstract class Worker extends Thread implements Runnable {

    public static class WorkerCallback {
        public void onComplete(boolean cancelled){}
        public void onError(DataFordelerException e) {}
    }

    protected boolean doCancel = false;
    protected WorkerCallback callback = null;

    public void end() {
        System.out.println("setting doCancel");
        this.doCancel = true;
    }

    public void setCallback(WorkerCallback callback) {
        this.callback = callback;
    }

    protected void onComplete() {
        if (this.callback != null) {
            try {
                this.callback.onComplete(this.doCancel);
            } catch (Exception ex) {}
        }
    }

    protected void onError(DataFordelerException e) {
        if (this.callback != null) {
            try {
                this.callback.onError(e);
            } catch (Exception ex) {}
        }
    }

    public boolean isDoCancel() {
        return doCancel;
    }
}
