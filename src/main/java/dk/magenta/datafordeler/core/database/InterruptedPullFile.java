package dk.magenta.datafordeler.core.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="interrupted_pull_file")
public class InterruptedPullFile extends DatabaseEntry {

    public InterruptedPullFile(InterruptedPull interruptedPull, String file) {
        this.interruptedPull = interruptedPull;
        this.file = file;
    }

    @ManyToOne(targetEntity = InterruptedPull.class, optional = false)
    private InterruptedPull interruptedPull;

    @Column(length = 1024, nullable = false, name = "filename")
    private String file;

    public String getFile() {
        return this.file;
    }

}
