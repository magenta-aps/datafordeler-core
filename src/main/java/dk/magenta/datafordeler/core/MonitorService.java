package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.database.ConfigurationSessionManager;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.core.database.QueryManager;
import dk.magenta.datafordeler.core.database.SessionManager;
import dk.magenta.datafordeler.core.plugin.EntityManager;
import dk.magenta.datafordeler.core.plugin.Plugin;
import dk.magenta.datafordeler.core.plugin.RegisterManager;
import dk.magenta.datafordeler.core.util.CronUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashMap;

@Controller
@RequestMapping(path="/monitor")
public class MonitorService {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    ConfigurationSessionManager configurationSessionManager;

    @Autowired
    PluginManager pluginManager;

    @RequestMapping(path="/database")
    public void checkDatabaseConnections(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        Query query = session.createQuery("select 1 from Identification").setMaxResults(1);
        query.uniqueResult();
        session.close();
        response.getWriter().println("Primary database connection ok");

        session = configurationSessionManager.getSessionFactory().openSession();
        query = session.createQuery("select 1 from Command").setMaxResults(1);
        query.uniqueResult();
        session.close();
        response.getWriter().println("Secondary database connection ok");

        response.setStatus(200);
    }

    @RequestMapping(path="/pull")
    public void checkPulls(HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException {
        Session session = sessionManager.getSessionFactory().openSession();
        PrintWriter output = response.getWriter();
        for (Plugin plugin : pluginManager.getPlugins()) {
            RegisterManager registerManager = plugin.getRegisterManager();
            String pullSchedule = CronUtil.reformatSchedule(registerManager.getPullCronSchedule());
            if (pullSchedule != null && !pullSchedule.isEmpty()) {
                CronExpression cronExpression = new CronExpression(pullSchedule);
                for (EntityManager entityManager : registerManager.getEntityManagers()) {
                    // Check that entitymanagers that should run on a cron job
                    // have completed their expected pulls within, say, 2 hours
                    // to detect stalled jobs
                    output.println("Inspecting "+entityManager.getClass().getSimpleName());

                    // When does cron say we should have started last?
                    Instant expectedStart = MonitorService.getTimeBefore(cronExpression, Instant.now());
                    output.println("    Expecting last start at "+expectedStart.toString());

                    // When did we start last
                    OffsetDateTime lastStartTime = entityManager.getLastUpdated(session);
                    output.println("    Last start was at "+expectedStart.toString());

                    // fail if more than two hours have passed since expectedstart and we are not yet done
                    // not yet done = lastStartTime is somewhere before expectedStart
                    if (
                            Instant.now().plus(2, ChronoUnit.HOURS).isAfter(expectedStart) &&
                                    (lastStartTime == null || lastStartTime.toInstant().isBefore(expectedStart))
                            ) {
                        output.println("It is more than 2 hours after expected start, and last start has not been updated to be after expected start");
                        response.setStatus(500);
                    }

                }
            }
        }
    }

    // Because org.quartz.CronExpression does not have getTimeBefore implemented
    public static Instant getTimeBefore(CronExpression cronExpression, Instant date) {
        Instant someTimeBefore = null;

        // Go back from date until we pass at least one instance where the cron would fire
        long a = 1;
        Instant d = date;
        for (int i=0; i<1000 && someTimeBefore == null; i++) {
            Instant earlier = d.minus(a, ChronoUnit.SECONDS);
            Date cronDate = cronExpression.getTimeAfter(
                    Date.from(earlier)
            );
            //System.out.println("Cron fires at "+cronDate.toInstant().toString());
            if (cronDate.after(Date.from(d))) {
                //System.out.println("That's after our start, go further back");
                d = earlier;
            } else {
                //System.out.println("That's before our start. We're happy");
                someTimeBefore = earlier;
            }
            a *= 2;
        }
        if (someTimeBefore == null) {
            return null;
        }

        // Go forward in cron fire times until we hit our original date
        Date cronDate = Date.from(someTimeBefore);
        Date end = Date.from(date);
        while (true) {
            Date next = cronExpression.getTimeAfter(cronDate);
            //System.out.println("Next fire time after "+cronDate.toInstant().toString()+" is "+next.toInstant().toString());
            if (!next.before(end)) {
                break;
            }
            cronDate = next;
        }
        return cronDate.toInstant();
    }

}
