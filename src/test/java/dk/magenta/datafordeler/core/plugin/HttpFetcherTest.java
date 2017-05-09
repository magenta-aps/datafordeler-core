package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.core.AppConfig;
import dk.magenta.datafordeler.core.exception.DataStreamException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by lars on 09-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class HttpFetcherTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testFetchOk() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Fetcher fetcher = new HttpFetcher();
        InputStream data = fetcher.fetch(new URI("https://www.example.com"));
        Assert.assertNotNull(data);
        Assert.assertNotNull(data.read(new byte[10]));
    }

    @Test
    public void testFetchFail1() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Fetcher fetcher = new HttpFetcher();
        exception.expect(DataStreamException.class);
        fetcher.fetch(new URI("https://frsghr8hffdh0gtonxhpjd.gl"));
    }

    @Test
    public void testFetchFail2() throws URISyntaxException, HttpStatusException, DataStreamException, IOException {
        Fetcher fetcher = new HttpFetcher();
        exception.expect(HttpStatusException.class);
        fetcher.fetch(new URI("https://www.example.com/foo"));
    }
}
