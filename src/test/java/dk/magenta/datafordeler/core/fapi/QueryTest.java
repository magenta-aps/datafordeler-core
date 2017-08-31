package dk.magenta.datafordeler.core.fapi;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Entity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 05-05-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class)
public class QueryTest {

    private class QueryImpl extends Query<Entity> {

        public QueryImpl() {
            super();
        }

        public QueryImpl(int page, int pageSize) {
            super(page, pageSize);
        }

        @Override
        public Map<String, Object> getSearchParameters() {
            return new HashMap<>();
        }

        @Override
        public void setFromParameters(ParameterMap parameters) {
        }

        @Override
        public Class<Entity> getEntityClass() {
            return Entity.class;
        }

        @Override
        public Class getDataClass() {
            return DataItem.class;
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testPagesize() throws Exception {
        Query query = new QueryImpl(1, 10);
        Assert.assertEquals(10, query.getPageSize());
        query.setPageSize(20);
        Assert.assertEquals(20, query.getPageSize());
        query.setPageSize("30");
        Assert.assertEquals(30, query.getPageSize());
        query.setPageSize(null);
        Assert.assertEquals(30, query.getPageSize());
    }

    @Test
    public void testPagesizeFail() throws Exception {
        exception.expect(IllegalArgumentException.class);
        Query query = new QueryImpl(1, 0);
    }

    @Test
    public void testPage() throws Exception {
        Query query = new QueryImpl(1, 10);
        Assert.assertEquals(1, query.getPage());
        query.setPage(2);
        Assert.assertEquals(2, query.getPage());
        query.setPage("3");
        Assert.assertEquals(3, query.getPage());
        query.setPage(null);
        Assert.assertEquals(3, query.getPage());
    }

    @Test
    public void testPageFail() throws Exception {
        exception.expect(IllegalArgumentException.class);
        Query query = new QueryImpl(-1, 1);
    }

    @Test
    public void testOffset() {
        Query query = new QueryImpl(2, 10);
        Assert.assertEquals(10, query.getOffset());
    }

    @Test
    public void testCount() {
        Query query = new QueryImpl(1, 10);
        Assert.assertEquals(10, query.getCount());
    }

    private HashMap<String, String> dateTimeTests = new HashMap<>();

    @Before
    public void populateDateTimeTests() {
        this.dateTimeTests.put("2017-05-05T13:30:00+01:00", "2017-05-05T13:30:00+01:00");
        this.dateTimeTests.put("2017-05-05+01:00", "2017-05-05T00:00:00+01:00");
        this.dateTimeTests.put("2017-05-05T14:07:30+01:00[Europe/Copenhagen]", "2017-05-05T14:07:30+01:00");
        this.dateTimeTests.put("2017-05-05T14:10:30Z", "2017-05-05T14:10:30+00:00");
        this.dateTimeTests.put("Fri, 5 May 2017 14:11:30 GMT", "2017-05-05T14:11:30+00:00");
        this.dateTimeTests.put("2017-05-05", "2017-05-05T00:00:00+00:00");
        this.dateTimeTests.put("20170505", "2017-05-05T00:00:00+00:00");
    }

    @Test
    public void testRegistrationFrom() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setRegistrationFrom(time);
        Assert.assertEquals(time, query.getRegistrationFrom());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setRegistrationFrom(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getRegistrationFrom());
        }
    }

    @Test
    public void testRegistrationTo() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setRegistrationTo(time);
        Assert.assertEquals(time, query.getRegistrationTo());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setRegistrationTo(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getRegistrationTo());
        }
    }

    @Test
    public void testEffectFrom() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setEffectFrom(time);
        Assert.assertEquals(time, query.getEffectFrom());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setEffectFrom(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getEffectFrom());
        }
    }

    @Test
    public void testEffectTo() throws Exception {
        Query query = new QueryImpl();
        OffsetDateTime time = OffsetDateTime.now();
        query.setEffectTo(time);
        Assert.assertEquals(time, query.getEffectTo());

        for (String testDateTime : this.dateTimeTests.keySet()) {
            query.setEffectTo(testDateTime);
            Assert.assertEquals(OffsetDateTime.parse(this.dateTimeTests.get(testDateTime)), query.getEffectTo());
        }
    }

    @Test
    public void testGetSearchParameters() {
        Query query = new QueryImpl();
        Assert.assertNotNull(query.getSearchParameters());
        Assert.assertEquals(0, query.getSearchParameters().size());
    }
}
