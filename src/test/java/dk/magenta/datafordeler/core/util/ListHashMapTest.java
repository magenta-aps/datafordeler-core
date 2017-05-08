package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.AppConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by lars on 05-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ListHashMapTest {
    @Test
    public void testAdd() {
        ListHashMap<String, String> map = new ListHashMap<>();
        String key = "foo";
        String value = "bar";
        map.add(key, value);
        List<String> list = map.get(key);
        Assert.assertTrue(list.contains(value));
    }

    @Test
    public void testGet() {
        ListHashMap<String, String> map = new ListHashMap<>();
        String key = "foo";
        String value1 = "bar";
        String value2 = "baz";
        map.add(key, value1);
        map.add(key, value2);
        Assert.assertEquals(value1, map.get(key, 0));
        Assert.assertEquals(value2, map.get(key, 1));
    }
}
