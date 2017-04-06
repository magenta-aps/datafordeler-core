package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.exception.DataFordelerException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * Created by lars on 06-04-17.
 */
public class PullTask implements Job {

    public static final String DATA_REGISTERMANAGER = "registerManager";
    public static final String DATA_DUMMYRUN = "dummyRun";

    private RegisterManager registerManager;

    public PullTask() {
    }

    @Override
    public void execute(JobExecutionContext executionContext) {
        JobDataMap dataMap = executionContext.getMergedJobDataMap();
        RegisterManager registerManager = (RegisterManager) dataMap.get(DATA_REGISTERMANAGER);
        boolean dummyRun = dataMap.getBoolean(DATA_DUMMYRUN);
        if (!dummyRun) {
            this.pull(registerManager);
        }
    }

    protected void pull(RegisterManager registerManager) {
        try {
            registerManager.pullEvents();
        } catch (DataFordelerException e) {
            e.printStackTrace();
        }
    }
}
