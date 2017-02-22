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
        // testEntity = TestEntity.get(session, uuid, domain);
        testEntity = queryManager.getEntity(session, uuid, domain, TestEntity.class);
        Assert.assertNotNull(testEntity);
        Assert.assertEquals(uuid, testEntity.getUUID());
        Assert.assertEquals(domain, testEntity.getDomain());
        Identification identification = queryManager.getIdentification(session, uuid, domain, TestIdentification.class);
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
        transaction.commit();
        session.close();
    }
}
