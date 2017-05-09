package dk.magenta.datafordeler.core.database;

import dk.magenta.datafordeler.core.database.Entity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_entity")
public class TestEntity extends Entity<TestEntity, TestRegistration> {

    public TestEntity() {}

    public TestEntity(UUID uuid, String domain) {
        super(uuid, domain);
    }

    public static TestEntity get(Session session, UUID uuid, String domain) {
        Query<TestEntity> query = session.createQuery("select e from TestEntity e join e.identification i where i.uuid = :uuid and i.domain = :domain", TestEntity.class);
        query.setParameter("uuid", uuid);
        query.setParameter("domain", domain);
        return query.getSingleResult();
    }
}
