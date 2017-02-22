package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.Entity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_entity")
public class TestEntity extends Entity<TestIdentification, TestEntity, TestRegistration, TestEffect> {

    public TestEntity() {}

    public TestEntity(UUID uuid, String domain) {
        super(new TestIdentification(uuid, domain));
    }

    public static TestEntity get(Session session, long id) {
        return session.get(TestEntity.class, id);
    }

    public static TestEntity get(Session session, UUID uuid, String domain) {
        Query<TestEntity> query = session.createQuery("select e from TestEntity e join e.identification i where i.id = :id and i.domain = :domain", TestEntity.class);
        query.setParameter("id", uuid);
        query.setParameter("domain", domain);
        return query.getSingleResult();
    }
}
