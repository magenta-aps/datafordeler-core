package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Created by lars on 08-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class EventTest {
    @Test
    public void testBeskedID() {
        Event event = new Event();
        UUID uuid = UUID.randomUUID();
        event.setEventID(uuid.toString());
        Assert.assertEquals(uuid.toString(), event.getEventID());
    }
    @Test
    public void testBeskedversion() {
        Event event = new Event();
        event.setBeskedVersion("1.0");
        Assert.assertEquals("1.0", event.getBeskedVersion());
    }
    @Test
    public void testDataSkema() {
        Event event = new Event();
        event.setSchema("testskema");
        Assert.assertEquals("testskema", event.getSchema());
    }
    @Test
    public void testObjectData() {
        Event event = new Event();
        event.setData("{\"test\":42}");
        Assert.assertEquals("{\"test\":42}", event.getData());
    }
    @Test
    public void testObjektReference() {
        Event event = new Event();
        event.setObjektReference("{\"test\":42}");
        Assert.assertEquals("{\"test\":42}", event.getObjektReference());
    }
}
