package dk.magenta.datafordeler.core.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationPid;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
public class MonitorLogger {

    private Logger log = LogManager.getLogger(MonitorLogger.class);

    private static MonitorLogger instance;

    public MonitorLogger() {
        instance = this;
    }

    public static MonitorLogger getInstance() {
        return instance;
    }

    @PostConstruct
    public void init() throws IOException {
        this.createErrorFile();
    }


    @Value("${dafo.error_folder:errors}")
    private String errorFolderPath;

    private File errorFile = null;

    private static final String errorFileExtension = "err";

    public static String getErrorFilePath() {
        return getInstance().errorFolderPath + File.separator + new ApplicationPid().toString() + "." + errorFileExtension;
    }

    private void createErrorFile() throws IOException {
        File errorFolder = new File(this.errorFolderPath);
        if (!errorFolder.isDirectory()) {
            errorFolder.mkdirs();
        }
        this.errorFile = new File(this.getErrorFilePath());
        this.errorFile.createNewFile();
        this.log.info("Monitor Log file created at "+this.errorFile.getAbsolutePath());
    }

    public static File getErrorFile() {
        return getInstance().errorFile;
    }

    public static void logMonitoredError(Throwable e) {
        File errorFile = getErrorFile();
        try {
            PrintWriter writer = new PrintWriter(errorFile);
            writer.append("================================================================================");
            writer.append(OffsetDateTime.now().toString());
            e.printStackTrace(writer);
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
