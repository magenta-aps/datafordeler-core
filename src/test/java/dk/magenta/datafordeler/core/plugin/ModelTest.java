package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.plugindemo.model.DemoData;
import dk.magenta.datafordeler.plugindemo.model.DemoEffect;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import dk.magenta.datafordeler.plugindemo.model.DemoRegistration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lars on 02-06-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ModelTest {

    @Test
    public void testEntity() {

        DemoEntity demoEntity1 = new DemoEntity();
        Identification identification1 = new Identification(UUID.randomUUID(), "test");
        demoEntity1.setIdentifikation(identification1);

        // Test getIdentification
        Assert.assertEquals(identification1, demoEntity1.getIdentifikation());

        // Test getDomaene
        Assert.assertEquals(identification1.getDomaene(), demoEntity1.getDomain());

        // Test getUUID
        Assert.assertEquals(identification1.getUuid(), demoEntity1.getUUID());


        Identification identification2 = new Identification(UUID.randomUUID(), "test");
        DemoEntity demoEntity2 = new DemoEntity(identification2);

        // Test getIdentification
        Assert.assertEquals(identification2, demoEntity2.getIdentifikation());

        // Test getDomaene
        Assert.assertEquals(identification2.getDomaene(), demoEntity2.getDomain());

        // Test getUUID
        Assert.assertEquals(identification2.getUuid(), demoEntity2.getUUID());


        UUID uuid3 = UUID.randomUUID();
        String domain3 = "test";
        DemoEntity demoEntity3 = new DemoEntity(uuid3, domain3);

        // Test getDomaene
        Assert.assertEquals(domain3, demoEntity3.getDomain());

        // Test getUUID
        Assert.assertEquals(uuid3, demoEntity3.getUUID());

        UUID uuid4 = UUID.randomUUID();
        String domain4 = "demo";
        DemoEntity demoEntity4 = demoEntity3;
        demoEntity4.setDomain(domain4);
        demoEntity4.setUUID(uuid4);

        // Test getDomaene
        Assert.assertEquals(domain4, demoEntity4.getDomain());

        // Test getUUID
        Assert.assertEquals(uuid4, demoEntity4.getUUID());

        // Test getRegistrations
        DemoRegistration demoRegistration = new DemoRegistration();
        demoEntity1.addRegistration(demoRegistration);
        Assert.assertEquals(1, demoEntity1.getRegistreringer().size());
        Assert.assertEquals(demoRegistration, demoEntity1.getRegistreringer().iterator().next());
    }

    @Test
    public void testRegistration() {
        String registerChecksum = "7A07CD32F33B66B468C1E1FEA19B35EBF13406E0B4A21BD7A5CFEF323D3E7BD0";
        DemoEntity demoEntity = new DemoEntity();
        DemoRegistration demoRegistration1 = new DemoRegistration(OffsetDateTime.parse("2017-06-02T17:00:00+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"), 0);
        demoRegistration1.setEntity(demoEntity);
        demoRegistration1.setRegisterChecksum(registerChecksum);
        DemoEffect demoEffect1 = new DemoEffect(demoRegistration1, OffsetDateTime.parse("2017-06-02T17:00:00+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"));
        DemoData demoData1 = new DemoData(8000, "Århus C");
        demoData1.addVirkning(demoEffect1);

        // Test getVirkninger
        Assert.assertEquals(1, demoRegistration1.getVirkninger().size());
        Assert.assertTrue(demoRegistration1.getVirkninger().contains(demoEffect1));

        // Test getEffect
        Assert.assertEquals(demoEffect1, demoRegistration1.getEffect(OffsetDateTime.parse("2017-06-02T17:00:00+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00")));
        Assert.assertNotEquals(demoEffect1, demoRegistration1.getEffect(OffsetDateTime.parse("2017-06-02T17:30:00+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00")));
        Assert.assertNotEquals(demoEffect1, demoRegistration1.getEffect(OffsetDateTime.parse("2017-06-02T17:00:00+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+01:00")));

        // Test getEntity
        Assert.assertEquals(demoEntity, demoRegistration1.getEntity());
        Assert.assertTrue(demoEntity.getRegistreringer().contains(demoRegistration1));

        // Test getSekvensnummer
        Assert.assertEquals(0, demoRegistration1.getSekvensnummer());

        // Test getRegisterChecksum
        Assert.assertEquals(registerChecksum, demoRegistration1.getRegisterChecksum());

        // Test getRegistreringFra
        Assert.assertEquals(OffsetDateTime.parse("2017-06-02T17:00:00+02:00"), demoRegistration1.getRegistreringFra());

        // Test getRegistreringTil
        Assert.assertEquals(OffsetDateTime.parse("2017-06-05T08:00:16+02:00"), demoRegistration1.getRegistreringTil());

        // Test toString
        Assert.assertEquals("DemoRegistration["+demoRegistration1.hashCode()+"] {\n" +
                "    entity: null @ null\n" +
                "    checksum: 7A07CD32F33B66B468C1E1FEA19B35EBF13406E0B4A21BD7A5CFEF323D3E7BD0\n" +
                "    from: 2017-06-02T17:00+02:00\n" +
                "    to: 2017-06-05T08:00:16+02:00\n" +
                "    virkninger: [\n" +
                "        DemoEffect["+demoEffect1.hashCode()+"] {\n" +
                "            from: 2017-06-02T17:00+02:00\n" +
                "            to: 2017-06-05T08:00:16+02:00\n" +
                "            data: [\n" +
                "                DemoData["+demoData1.hashCode()+"] {\n" +
                "                    aktiv: true\n" +
                "                    bynavn: Århus C\n" +
                "                    postnr: 8000\n" +
                "                    reference: null\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}", demoRegistration1.toString());
        Assert.assertEquals("        DemoRegistration["+demoRegistration1.hashCode()+"] {\n" +
                "            entity: null @ null\n" +
                "            checksum: 7A07CD32F33B66B468C1E1FEA19B35EBF13406E0B4A21BD7A5CFEF323D3E7BD0\n" +
                "            from: 2017-06-02T17:00+02:00\n" +
                "            to: 2017-06-05T08:00:16+02:00\n" +
                "            virkninger: [\n" +
                "                DemoEffect["+demoEffect1.hashCode()+"] {\n" +
                "                    from: 2017-06-02T17:00+02:00\n" +
                "                    to: 2017-06-05T08:00:16+02:00\n" +
                "                    data: [\n" +
                "                        DemoData["+demoData1.hashCode()+"] {\n" +
                "                            aktiv: true\n" +
                "                            bynavn: Århus C\n" +
                "                            postnr: 8000\n" +
                "                            reference: null\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }", demoRegistration1.toString(2));
    }

    @Test
    public void testEffect() {
        DemoRegistration demoRegistration1 = new DemoRegistration();
        DemoRegistration demoRegistration2 = new DemoRegistration();

        // Test constructors
        Assert.assertTrue(
                new DemoEffect(demoRegistration1, ZonedDateTime.parse("2017-06-02T15:39:16+02:00"), ZonedDateTime.parse("2017-06-05T08:00:16+02:00"))
                        .equalData(
                                new DemoEffect(demoRegistration1, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))
                        )
        );
        Assert.assertTrue(
                new DemoEffect(demoRegistration1, "2017-06-02T15:39:16+02:00", "2017-06-05T08:00:16+02:00")
                        .equalData(
                                new DemoEffect(demoRegistration1, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))
                        )
        );

        // Test equalData
        DemoEffect demoEffect1 = new DemoEffect(demoRegistration1, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"));
        Assert.assertTrue(demoEffect1.equalData(new DemoEffect(demoRegistration1, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))));
        Assert.assertTrue(demoEffect1.equalData(new DemoEffect(demoRegistration2, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))));
        Assert.assertFalse(demoEffect1.equalData(new DemoEffect(demoRegistration2, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), OffsetDateTime.parse("2017-06-05T09:00:16+02:00"))));
        Assert.assertFalse(demoEffect1.equalData(new DemoEffect(demoRegistration2, OffsetDateTime.parse("2017-06-02T16:00:00+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))));
        Assert.assertFalse(demoEffect1.equalData(new DemoEffect(demoRegistration1, null, OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))));
        Assert.assertFalse(demoEffect1.equalData(new DemoEffect(demoRegistration1, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), null)));

        DemoEffect demoEffect3 = new DemoEffect();
        DemoRegistration demoRegistration3 = new DemoRegistration();
        Assert.assertFalse(demoEffect3.equalData(new DemoEffect(demoRegistration3, OffsetDateTime.parse("2017-06-02T16:00:00+02:00"), OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))));
        Assert.assertFalse(demoEffect3.equalData(new DemoEffect(demoRegistration3, null, OffsetDateTime.parse("2017-06-05T08:00:16+02:00"))));
        Assert.assertFalse(demoEffect3.equalData(new DemoEffect(demoRegistration3, OffsetDateTime.parse("2017-06-02T15:39:16+02:00"), null)));
        Assert.assertTrue(demoEffect3.equalData(new DemoEffect()));

        // Test getRegistrering
        Assert.assertEquals(demoRegistration1, demoEffect1.getRegistrering());
        Assert.assertNotEquals(demoRegistration2, demoEffect1.getRegistrering());

        // Test getTableName
        Assert.assertEquals("demo_effect", Effect.getTableName(DemoEffect.class));

        // Test toString
        DemoData demoData1 = new DemoData(8000, "Århus C");
        demoData1.addVirkning(demoEffect1);
        Assert.assertEquals("DemoEffect["+demoEffect1.hashCode()+"] {\n" +
                "    from: 2017-06-02T15:39:16+02:00\n" +
                "    to: 2017-06-05T08:00:16+02:00\n" +
                "    data: [\n" +
                "        DemoData["+demoData1.hashCode()+"] {\n" +
                "            aktiv: true\n" +
                "            bynavn: Århus C\n" +
                "            postnr: 8000\n" +
                "            reference: null\n" +
                "        }\n" +
                "    ]\n" +
                "}", demoEffect1.toString());
        Assert.assertEquals("        DemoEffect["+demoEffect1.hashCode()+"] {\n" +
                "            from: 2017-06-02T15:39:16+02:00\n" +
                "            to: 2017-06-05T08:00:16+02:00\n" +
                "            data: [\n" +
                "                DemoData["+demoData1.hashCode()+"] {\n" +
                "                    aktiv: true\n" +
                "                    bynavn: Århus C\n" +
                "                    postnr: 8000\n" +
                "                    reference: null\n" +
                "                }\n" +
                "            ]\n" +
                "        }", demoEffect1.toString(2));

        // Test getData
        Map<String, Object> data = demoEffect1.getData();
        Assert.assertEquals("Århus C", data.get("bynavn"));
        Assert.assertEquals(8000, data.get("postnr"));
        Assert.assertEquals(null, data.get("reference"));
        Assert.assertEquals(4, data.keySet().size());
    }

    @Test
    public void testDataItem() {
        DemoData demoData1 = new DemoData(8000, "Århus C");
        DemoData demoData2 = new DemoData(8000, "Århus C");
        DemoData demoData3 = new DemoData(8200, "Århus N");
        // Test equalData
        Assert.assertTrue(demoData1.equalData(demoData2));
        Assert.assertTrue(demoData2.equalData(demoData1));
        Assert.assertFalse(demoData1.equalData(demoData3));
        Assert.assertFalse(demoData3.equalData(demoData1));
        Assert.assertFalse(demoData2.equalData(demoData3));
        Assert.assertFalse(demoData3.equalData(demoData2));

        // Test getTableName
        Assert.assertEquals("demo_data", DataItem.getTableName(DemoData.class));

        // Test toString
        Assert.assertEquals("DemoData["+demoData1.hashCode()+"] {\n" +
                "    aktiv: true\n" +
                "    bynavn: Århus C\n" +
                "    postnr: 8000\n" +
                "    reference: null\n" +
                "}", demoData1.toString());
        Assert.assertEquals("        DemoData["+demoData1.hashCode()+"] {\n" +
                "            aktiv: true\n" +
                "            bynavn: Århus C\n" +
                "            postnr: 8000\n" +
                "            reference: null\n" +
                "        }", demoData1.toString(2));


        // Test getReferences
        Identification identification = new Identification(UUID.randomUUID(), "test");
        demoData1.setReference(identification);
        Assert.assertEquals(identification, demoData1.getReference());
        Assert.assertEquals(1, demoData1.getReferences().size());
        Assert.assertTrue(demoData1.getReferences().containsValue(identification));

        // Test updateReferences
        Identification identification2 = new Identification(UUID.randomUUID(), "test");
        HashMap<String, Identification> newReferences = new HashMap<>();
        newReferences.put("reference", identification2);
        demoData1.updateReferences(newReferences);
        Assert.assertEquals(identification2, demoData1.getReference());
        Assert.assertNotEquals(identification, demoData1.getReference());
        Assert.assertEquals(1, demoData1.getReferences().size());
        Assert.assertTrue(demoData1.getReferences().containsValue(identification2));
    }
}
