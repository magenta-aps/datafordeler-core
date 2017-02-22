package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.AppConfig;
import dk.magenta.datafordeler.core.QueryManager;
import dk.magenta.datafordeler.core.SessionManager;
import dk.magenta.datafordeler.core.model.Identification;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Created by lars on 21-02-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class DatabaseTest {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    QueryManager queryManager;

    private static final String domain = "test";

    @Test
    public void testEntity() {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        TestEntity testEntity = new TestEntity(uuid, domain);
        session.save(testEntity);
        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        testEntity = queryManager.getEntity(session, uuid, TestEntity.class);
        Assert.assertNotNull(testEntity);
        Assert.assertEquals(uuid, testEntity.getUUID());
        Assert.assertEquals(domain, testEntity.getDomain());
        Identification identification = queryManager.getIdentification(session, uuid, Identification.class);
        Assert.assertNotNull(identification);
        Assert.assertEquals(uuid, identification.getUuid());
        Assert.assertEquals(domain, identification.getDomain());
        session.delete(testEntity);
        transaction.commit();
        session.close();
    }

    @Test
    public void testRegistration() {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        TestEntity testEntity = new TestEntity(uuid, domain);
        session.save(testEntity);
        TestRegistration testRegistration = new TestRegistration(testEntity, "2017-02-21T16:02:50+01:00", null);
        session.save(testRegistration);
        Assert.assertTrue(testEntity.getRegistrations().contains(testRegistration));
        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        testRegistration = (TestRegistration) session.merge(testRegistration);
        transaction = session.beginTransaction();
        testEntity = queryManager.getEntity(session, uuid, TestEntity.class);
        Assert.assertTrue(testEntity.getRegistrations().contains(testRegistration));
        transaction.commit();
        session.close();
    }

    @Test
    public void testEffect() {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        TestEntity testEntity = new TestEntity(uuid, domain);
        session.save(testEntity);
        TestRegistration testRegistration = new TestRegistration(testEntity, "2017-02-21T16:02:50+01:00", null);
        session.save(testRegistration);
        TestEffect testEffect = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        session.save(testEffect);
        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();
        testEffect = (TestEffect) session.merge(testEffect);
        testEntity = queryManager.getEntity(session, uuid, TestEntity.class);
        boolean found = false;
        for (TestRegistration registration : testEntity.getRegistrations()) {
            for (TestEffect effect : registration.getEffects()) {
                if (effect == testEffect) {
                    found = true;
                }
            }
        }
        Assert.assertTrue(found);
        transaction.commit();
        session.close();
    }
}
