package dk.magenta.datafordeler.core.exception;

import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class EntityManagerNotFoundExceptionTest {

    @Test
    public void testEntityManagerNotFoundException() throws Exception {
        EntityManagerNotFoundException exception1 = new EntityManagerNotFoundException(DemoEntity.schema);
        Assert.assertEquals("EntityManager that handles schema " + DemoEntity.schema + " was not found", exception1.getMessage());

        URI uri = new URI("https://data.gl");
        EntityManagerNotFoundException exception2 = new EntityManagerNotFoundException(uri);
        Assert.assertEquals("EntityManager that handles URI https://data.gl was not found", exception2.getMessage());

        Assert.assertEquals("datafordeler.plugin.entitymanager_not_found", exception1.getCode());
    }

}
