package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.AppConfig;
import dk.magenta.datafordeler.core.exception.InvalidDataInputException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
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
    public void testRegistration() throws InvalidDataInputException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration = new TestRegistration("2017-02-21T16:02:50+01:00", null, 1);
        queryManager.saveRegistration(session, testEntity, testRegistration);
        session.flush();
        transaction.commit();
        Assert.assertTrue(testEntity.getRegistrations().contains(testRegistration));
        session.close();
        session = sessionManager.getSessionFactory().openSession();
        testRegistration = (TestRegistration) session.merge(testRegistration);
        transaction = session.beginTransaction();
        testEntity = queryManager.getEntity(session, uuid, TestEntity.class);
        Assert.assertNotNull(testEntity);
        Assert.assertEquals(uuid, testEntity.getUUID());
        Assert.assertEquals(domain, testEntity.getDomain());
        Identification identification = queryManager.getIdentification(session, uuid);
        Assert.assertNotNull(identification);
        Assert.assertEquals(uuid, identification.getUuid());
        Assert.assertEquals(domain, identification.getDomain());
        Assert.assertTrue(testEntity.getRegistrations().contains(testRegistration));
        transaction.commit();
        session.close();
    }

    @Test
    public void testEffect() throws InvalidDataInputException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration = new TestRegistration("2017-02-21T16:02:50+01:00", null, 1);
        TestEffect testEffect = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        queryManager.saveRegistration(session, testEntity, testRegistration);
        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        queryManager.getAllEntities(session, TestEntity.class);

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

        session = sessionManager.getSessionFactory().openSession();
        testEntity = (TestEntity) session.merge(testEntity);
        List<TestEffect> effects = queryManager.getEffects(session, testEntity, OffsetDateTime.parse("2017-02-22T13:59:30+01:00"), OffsetDateTime.parse("2017-12-31T23:59:59+01:00"), TestEffect.class);

        found = false;
        for (TestEffect effect : effects) {
            if (effect.getEffectFrom().equals(testEffect.getEffectFrom()) && effect.getEffectTo().equals(testEffect.getEffectTo())) {
                found = true;
            }
        }
        Assert.assertTrue(found);

        session.close();
    }

    @Test
    public void testDataItem() throws InvalidDataInputException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration = new TestRegistration("2017-02-21T16:02:50+01:00", null, 1);
        TestEffect testEffect = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestDataItem testDataItem = new TestDataItem(8000, "Århus");
        testDataItem.addEffect(testEffect);
        queryManager.saveRegistration(session, testEntity, testRegistration);
        session.flush();
        transaction.commit();
        //session.close();

        //session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        queryManager.getAllEntities(session, TestEntity.class);

        TestEntity testEntity1 = queryManager.getEntity(session, uuid, TestEntity.class);
        Assert.assertNotNull(testEntity1);
        boolean found = false;
        for (TestRegistration registration : testEntity1.getRegistrations()) {
            for (TestEffect effect : registration.getEffects()) {
                for (TestDataItem data : effect.getDataItems()) {
                    if (data.getPostnr() == 8000 && data.getBynavn().equals("Århus")) {
                        found = true;
                    }
                }
            }
        }
        Assert.assertTrue(found);

        testDataItem = (TestDataItem) session.merge(testDataItem);
        List<TestDataItem> results = queryManager.getDataItems(session, testEntity, testDataItem, TestDataItem.class);
        Assert.assertTrue(results.contains(testDataItem));
        List<TestDataItem> results1 = queryManager.getDataItems(session, testEntity, new TestDataItem(8000, "Århus"), TestDataItem.class);
        Assert.assertTrue(results1.contains(testDataItem));
        List<TestDataItem> results2 = queryManager.getDataItems(session, testEntity, new TestDataItem(8200, "Århus N"), TestDataItem.class);
        Assert.assertFalse(results2.contains(testDataItem));

        transaction.commit();

        /*transaction = session.beginTransaction();
        session.delete(testEntity);
        session.delete(testRegistration);
        transaction.commit();*/
        session.close();
    }

    @Test
    public void testDedup() throws InvalidDataInputException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        TestEntity testEntity = new TestEntity(uuid, domain);
        TestRegistration testRegistration = new TestRegistration("2017-02-21T16:02:50+01:00", null, 1);
        TestEffect testEffect1 = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestEffect testEffect2 = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestEffect testEffect3 = new TestEffect(testRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        TestEffect testEffect4 = new TestEffect(testRegistration, "2017-12-31T23:59:59+01:00", "2018-12-31T23:59:59+01:00");
        TestEffect testEffect5 = new TestEffect(testRegistration, "2017-12-31T23:59:59+01:00", "2018-12-31T23:59:59+01:00");

        queryManager.saveRegistration(session, testEntity, testRegistration);

        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        testRegistration = (TestRegistration) session.merge(testRegistration);

        queryManager.dedupEffects(session, testRegistration);
        List<TestEffect> testEffects = testRegistration.getEffects();

        Assert.assertEquals(2, testEffects.size());
        for (TestEffect e1 : testEffects) {
            for (TestEffect e2 : testEffects) {
                if (e1 != e2) {
                    Assert.assertFalse(e1.equalData(e2));
                }
            }
        }

        session.saveOrUpdate(testRegistration);

        transaction.commit();
        session.close();
    }
}
