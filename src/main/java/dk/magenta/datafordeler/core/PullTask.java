package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.MonitorLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;

public class PullTask implements InterruptableJob {

    public static final String DATA_ENGINE = "engine";
    public static final String DATA_REGISTERMANAGER = "registerManager";
    public static final String DATA_DUMMYRUN = "dummyRun";
    public static final String DATA_SCHEDULE = "schedule";

    private Logger log = LogManager.getLogger(PullTask.class);

    private Pull pull;

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
        Thread.UncaughtExceptionHandler exceptionHandler = (th, ex) -> {
            ex.printStackTrace();
            MonitorLogger.logMonitoredError(ex);
        };
        this.pull = new Pull(engine, registerManager);
        this.pull.setUncaughtExceptionHandler(exceptionHandler);
        this.pull.start();
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.pull.interrupt();
        try {
            this.pull.join();
        } catch (InterruptedException e) {
            throw new UnableToInterruptJobException(e);
        }
    }
}
