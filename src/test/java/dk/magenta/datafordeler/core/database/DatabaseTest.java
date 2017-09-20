package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import dk.magenta.datafordeler.plugindemo.fapi.DemoQuery;
import dk.magenta.datafordeler.plugindemo.model.DemoData;
import dk.magenta.datafordeler.plugindemo.model.DemoEffect;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
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
import java.util.UUID;

/**
 * Created by lars on 21-02-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class DatabaseTest {

    @Autowired
    SessionManager sessionManager;

    @Autowired
    QueryManager queryManager;

    private static final String domain = "test";


    @Test
    public void testRegistration() throws DataFordelerException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        DemoEntity demoEntity = new DemoEntity();
        Identification demoIdentification = new Identification(uuid, domain);
        demoEntity.setIdentifikation(demoIdentification);
        DemoRegistration demoRegistration = new DemoRegistration("2017-02-21T16:02:50+01:00", null, 1);
        queryManager.saveRegistration(session, demoEntity, demoRegistration);
        session.flush();
        transaction.commit();
        Assert.assertTrue(demoEntity.getRegistrations().contains(demoRegistration));
        session.close();
        session = sessionManager.getSessionFactory().openSession();
        demoRegistration = (DemoRegistration) session.merge(demoRegistration);
        transaction = session.beginTransaction();
        demoEntity = queryManager.getEntity(session, uuid, DemoEntity.class);
        Assert.assertNotNull(demoEntity);
        Assert.assertEquals(uuid, demoEntity.getUUID());
        Assert.assertEquals(domain, demoEntity.getDomain());
        Identification identification = queryManager.getIdentification(session, uuid);
        Assert.assertNotNull(identification);
        Assert.assertEquals(uuid, identification.getUuid());
        Assert.assertEquals(domain, identification.getDomain());
        Assert.assertTrue(demoEntity.getRegistrations().contains(demoRegistration));
        transaction.commit();
        session.close();
    }

    @Test
    public void testEffect() throws DataFordelerException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        DemoEntity demoEntity = new DemoEntity();
        Identification demoIdentification = new Identification(uuid, domain);
        demoEntity.setIdentifikation(demoIdentification);
        DemoRegistration demoRegistration = new DemoRegistration("2017-02-21T16:02:50+01:00", null, 1);
        DemoEffect demoEffect = new DemoEffect(demoRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        queryManager.saveRegistration(session, demoEntity, demoRegistration);
        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        queryManager.getAllEntities(session, DemoEntity.class);

        demoEffect = (DemoEffect) session.merge(demoEffect);
        demoEntity = queryManager.getEntity(session, uuid, DemoEntity.class);
        boolean found = false;
        for (DemoRegistration registration : demoEntity.getRegistrations()) {
            for (DemoEffect effect : registration.getEffects()) {
                if (effect == demoEffect) {
                    found = true;
                }
            }
        }
        Assert.assertTrue(found);
        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        demoEntity = (DemoEntity) session.merge(demoEntity);
        List<DemoEffect> effects = queryManager.getEffects(session, demoEntity, OffsetDateTime.parse("2017-02-22T13:59:30+01:00"), OffsetDateTime.parse("2017-12-31T23:59:59+01:00"), DemoEffect.class);

        found = false;
        for (DemoEffect effect : effects) {
            if (effect.getEffectFrom().equals(demoEffect.getEffectFrom()) && effect.getEffectTo().equals(demoEffect.getEffectTo())) {
                found = true;
            }
        }
        Assert.assertTrue(found);

        session.close();
    }

    @Test
    public void testDataItem() throws DataFordelerException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        DemoEntity demoEntity = new DemoEntity();
        Identification demoIdentification = new Identification(uuid, domain);
        demoEntity.setIdentifikation(demoIdentification);
        DemoRegistration demoRegistration = new DemoRegistration("2017-02-21T16:02:50+01:00", null, 1);
        DemoEffect demoEffect = new DemoEffect(demoRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        DemoData demoDataItem = new DemoData(8000, "Århus");
        demoDataItem.addEffect(demoEffect);
        Assert.assertTrue(demoEffect.getDataItems().contains(demoDataItem));
        demoDataItem.removeEffect(demoEffect);
        Assert.assertFalse(demoEffect.getDataItems().contains(demoDataItem));
        demoDataItem.addEffect(demoEffect);
        queryManager.saveRegistration(session, demoEntity, demoRegistration);
        transaction.commit();
        //session.close();

        //session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        queryManager.getAllEntities(session, DemoEntity.class);

        DemoEntity demoEntity1 = queryManager.getEntity(session, uuid, DemoEntity.class);
        Assert.assertNotNull(demoEntity1);
        boolean found = false;
        for (DemoRegistration registration : demoEntity1.getRegistrations()) {
            for (DemoEffect effect : registration.getEffects()) {
                for (DemoData data : effect.getDataItems()) {
                    if (data.getPostnr() == 8000 && data.getBynavn().equals("Århus")) {
                        found = true;
                    }
                }
            }
        }
        Assert.assertTrue(found);

        demoDataItem = (DemoData) session.merge(demoDataItem);
        List<DemoData> results = queryManager.getDataItems(session, demoEntity, demoDataItem, DemoData.class);
        System.out.println(results);
        Assert.assertTrue(results.contains(demoDataItem));
        List<DemoData> results1 = queryManager.getDataItems(session, demoEntity, new DemoData(8000, "Århus"), DemoData.class);
        Assert.assertTrue(results1.contains(demoDataItem));
        List<DemoData> results2 = queryManager.getDataItems(session, demoEntity, new DemoData(8200, "Århus N"), DemoData.class);
        Assert.assertFalse(results2.contains(demoDataItem));

        transaction.commit();
        session.close();
    }

    @Test
    public void testDedup() throws DataFordelerException {
        UUID uuid = UUID.randomUUID();
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        DemoEntity demoEntity = new DemoEntity();
        Identification demoIdentification = new Identification(uuid, domain);
        demoEntity.setIdentifikation(demoIdentification);
        DemoRegistration demoRegistration = new DemoRegistration("2017-02-21T16:02:50+01:00", null, 1);
        DemoEffect demoEffect1 = new DemoEffect(demoRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        DemoEffect demoEffect2 = new DemoEffect(demoRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        DemoEffect demoEffect3 = new DemoEffect(demoRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        DemoEffect demoEffect4 = new DemoEffect(demoRegistration, "2017-12-31T23:59:59+01:00", "2018-12-31T23:59:59+01:00");
        DemoEffect demoEffect5 = new DemoEffect(demoRegistration, "2017-12-31T23:59:59+01:00", "2018-12-31T23:59:59+01:00");

        queryManager.saveRegistration(session, demoEntity, demoRegistration);

        transaction.commit();
        session.close();

        session = sessionManager.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        demoRegistration = (DemoRegistration) session.merge(demoRegistration);

        queryManager.dedupEffects(session, demoRegistration);
        List<DemoEffect> demoEffects = demoRegistration.getEffects();

        Assert.assertEquals(2, demoEffects.size());
        for (DemoEffect e1 : demoEffects) {
            for (DemoEffect e2 : demoEffects) {
                if (e1 != e2) {
                    Assert.assertFalse(e1.equalData(e2));
                }
            }
        }

        session.saveOrUpdate(demoRegistration);

        transaction.commit();
        session.close();
    }

    @Test
    public void testGetAllEntities() throws DataFordelerException {
        Session session = sessionManager.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        UUID uuid = UUID.randomUUID();

        Identification identification = new Identification(uuid, domain);
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setIdentifikation(identification);
        DemoRegistration demoRegistration = new DemoRegistration("2017-02-21T16:02:50+01:00", null, 1);
        demoRegistration.setEntity(demoEntity);
        DemoEffect demoEffect = new DemoEffect(demoRegistration, "2017-02-22T13:59:30+01:00", "2017-12-31T23:59:59+01:00");
        DemoData demoData = new DemoData(1455, "København K");
        demoData.addEffect(demoEffect);
        DemoData demoData2 = new DemoData(9999, "NameWith%");
        demoData2.addEffect(demoEffect);
        queryManager.saveRegistration(session, demoEntity, demoRegistration);

        transaction.commit();

        DemoQuery demoQuery1 = new DemoQuery();
        demoQuery1.setPostnr(1455);
        List<DemoEntity> results1 = queryManager.getAllEntities(session, demoQuery1, DemoEntity.class);
        Assert.assertEquals(1, results1.size());
        Assert.assertEquals(uuid, results1.get(0).getUUID());

        DemoQuery demoQuery2 = new DemoQuery();
        demoQuery2.setPostnr("1455");
        List<DemoEntity> results2 = queryManager.getAllEntities(session, demoQuery2, DemoEntity.class);
        Assert.assertEquals(1, results2.size());
        Assert.assertEquals(uuid, results2.get(0).getUUID());

        DemoQuery demoQuery3 = new DemoQuery();
        demoQuery3.setPostnr("1*");
        List<DemoEntity> results3 = queryManager.getAllEntities(session, demoQuery3, DemoEntity.class);
        Assert.assertEquals(1, results3.size());
        Assert.assertEquals(uuid, results3.get(0).getUUID());

        DemoQuery demoQuery4 = new DemoQuery();
        demoQuery4.setPostnr("2*");
        List<DemoEntity> results4 = queryManager.getAllEntities(session, demoQuery4, DemoEntity.class);
        Assert.assertEquals(0, results4.size());

        DemoQuery demoQuery5 = new DemoQuery();
        demoQuery5.setBynavn("København K");
        demoQuery5.setAktiv("true");
        List<DemoEntity> results5 = queryManager.getAllEntities(session, demoQuery5, DemoEntity.class);
        Assert.assertEquals(1, results5.size());
        Assert.assertEquals(uuid, results5.get(0).getUUID());

        DemoQuery demoQuery6 = new DemoQuery();
        demoQuery6.setBynavn("København*");
        demoQuery5.setAktiv("yes");
        List<DemoEntity> results6 = queryManager.getAllEntities(session, demoQuery6, DemoEntity.class);
        Assert.assertEquals(1, results6.size());
        Assert.assertEquals(uuid, results6.get(0).getUUID());

        DemoQuery demoQuery7 = new DemoQuery();
        demoQuery7.setBynavn("Roskilde");
        List<DemoEntity> results7 = queryManager.getAllEntities(session, demoQuery7, DemoEntity.class);
        Assert.assertEquals(0, results7.size());

        DemoQuery demoQuery8 = new DemoQuery();
        demoQuery8.setBynavn("København K");
        demoQuery8.setPage(2);
        List<DemoEntity> results8 = queryManager.getAllEntities(session, demoQuery8, DemoEntity.class);
        Assert.assertEquals(0, results8.size());

        DemoQuery demoQuery9 = new DemoQuery();
        demoQuery9.setBynavn("København K");
        demoQuery9.setPageSize(1);
        List<DemoEntity> results9 = queryManager.getAllEntities(session, demoQuery9, DemoEntity.class);
        Assert.assertEquals(1, results9.size());
        Assert.assertEquals(uuid, results9.get(0).getUUID());

        DemoQuery demoQuery10 = new DemoQuery();
        demoQuery10.setBynavn("København K");
        demoQuery10.setAktiv("false");
        List<DemoEntity> results10 = queryManager.getAllEntities(session, demoQuery10, DemoEntity.class);
        Assert.assertEquals(0, results10.size());

        DemoQuery demoQuery11 = new DemoQuery();
        demoQuery11.setBynavn("NameWith%");
        List<DemoEntity> results11 = queryManager.getAllEntities(session, demoQuery11, DemoEntity.class);
        Assert.assertEquals(1, results11.size());

        DemoQuery demoQuery12 = new DemoQuery();
        demoQuery12.setBynavn("*With%");
        List<DemoEntity> results12 = queryManager.getAllEntities(session, demoQuery12, DemoEntity.class);
        Assert.assertEquals(1, results12.size());
    }
}
