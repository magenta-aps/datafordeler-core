package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.AppConfig;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Identification;
import dk.magenta.datafordeler.plugindemo.model.DemoData;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by lars on 02-06-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ModelTest {

    @Test
    public void testDataItem() {
        DemoData demoData1 = new DemoData(8000, "Århus C");
        DemoData demoData2 = new DemoData(8000, "Århus C");
        DemoData demoData3 = new DemoData(8200, "Århus N");
        Assert.assertTrue(demoData1.equalData(demoData2));
        Assert.assertTrue(demoData2.equalData(demoData1));
        Assert.assertFalse(demoData1.equalData(demoData3));
        Assert.assertFalse(demoData3.equalData(demoData1));
        Assert.assertFalse(demoData2.equalData(demoData3));
        Assert.assertFalse(demoData3.equalData(demoData2));

        Assert.assertEquals("demo_data", DataItem.getTableName(DemoData.class));

        Assert.assertEquals("DemoData["+demoData1.hashCode()+"] {\n" +
                "    bynavn: Århus C\n" +
                "    postnr: 8000\n" +
                "    reference: null\n" +
                "}", demoData1.toString());
        Assert.assertEquals("        DemoData["+demoData1.hashCode()+"] {\n" +
                "            bynavn: Århus C\n" +
                "            postnr: 8000\n" +
                "            reference: null\n" +
                "        }", demoData1.toString(2));


        Identification identification = new Identification(UUID.randomUUID(), "test");
        demoData1.setReference(identification);
        Assert.assertEquals(identification, demoData1.getReference());
        Assert.assertEquals(1, demoData1.getReferences().size());
        Assert.assertTrue(demoData1.getReferences().containsValue(identification));

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
