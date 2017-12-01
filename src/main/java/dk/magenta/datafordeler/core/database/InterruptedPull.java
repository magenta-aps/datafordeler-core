package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.plugin.Plugin;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name="interrupted_pull")
public class InterruptedPull extends DatabaseEntry {


    @Column
    private String plugin;

    public String getPlugin() {
        return this.plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin.getName();
    }


    @Column
    private String schemaName;

    public String getSchemaName() {
        return this.schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }



    @Column
    private OffsetDateTime startTime;

    public OffsetDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }



    @Column
    private OffsetDateTime interruptTime;

    public OffsetDateTime getInterruptTime() {
        return this.interruptTime;
    }

    public void setInterruptTime(OffsetDateTime interruptTime) {
        this.interruptTime = interruptTime;
    }



    @OneToMany(targetEntity = InterruptedPullFile.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "interruptedPull")
    private List<InterruptedPullFile> files = new ArrayList<>();

    public List<InterruptedPullFile> getFiles() {
        return this.files;
    }

    public void addFile(File file) {
        this.files.add(new InterruptedPullFile(this, file.getAbsolutePath()));
    }

    public void setFiles(Collection<File> files) {
        for (File file : files) {
            this.addFile(file);
        }
    }



    @Column
    private long chunk;

    public long getChunk() {
        return this.chunk;
    }

    public void setChunk(long chunk) {
        this.chunk = chunk;
    }
}
