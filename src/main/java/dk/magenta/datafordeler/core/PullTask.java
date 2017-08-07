package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.plugin.RegisterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * Created by lars on 06-04-17.
 */
public class PullTask implements Job {

    public static final String DATA_ENGINE = "engine";
    public static final String DATA_REGISTERMANAGER = "registerManager";
    public static final String DATA_DUMMYRUN = "dummyRun";
    public static final String DATA_SCHEDULE = "schedule";

    private Logger log = LogManager.getLogger("PullTask");

    public PullTask() {
    }

    @Override
    public void execute(JobExecutionContext executionContext) {
        JobDataMap dataMap = executionContext.getMergedJobDataMap();
        Engine engine = (Engine) dataMap.get(DATA_ENGINE);
        RegisterManager registerManager = (RegisterManager) dataMap.get(DATA_REGISTERMANAGER);
        boolean dummyRun = dataMap.getBoolean(DATA_DUMMYRUN);
        String schedule = dataMap.getString(DATA_SCHEDULE);
        if (!dummyRun) {
            this.log.info("Pulling events with "+registerManager.getClass().getCanonicalName()+" according to schedule ("+schedule+")");
            this.pull(engine, registerManager);
        }
    }

    protected void pull(Engine engine, RegisterManager registerManager) {
        Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                ex.printStackTrace();
            }
        };
        Pull pull = new Pull(engine, registerManager);
        pull.setUncaughtExceptionHandler(exceptionHandler);
        pull.start();
    }
}
