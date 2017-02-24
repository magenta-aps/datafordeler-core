package dk.magenta.datafordeler.core.plugin;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by lars on 11-01-17.
 */
public abstract class DataVersion {

    protected DataEntity entity;

    protected Collection<DataPiece> dataPieces;

    public abstract String getChecksum();

    public DataVersion() {
        this.dataPieces = new ArrayList<DataPiece>();
    }


    public DataEntity getEntity() {
        return entity;
    }
}
