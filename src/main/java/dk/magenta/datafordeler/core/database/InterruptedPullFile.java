package dk.magenta.datafordeler.core.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Single file entry for InterruptedPull
 */
@Entity
@Table(name="interrupted_pull_file")
public class InterruptedPullFile extends DatabaseEntry {

    public InterruptedPullFile() {
    }

    public InterruptedPullFile(InterruptedPull interruptedPull, String filename) {
        this.interruptedPull = interruptedPull;
        this.filename = filename;
    }

    @ManyToOne(targetEntity = InterruptedPull.class, optional = false)
    private InterruptedPull interruptedPull;

    @Column(length = 1024)
    private String filename;

    public String getFilename() {
        return this.filename;
    }

}
