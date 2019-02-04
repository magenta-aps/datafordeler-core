package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.*;
import org.hibernate.Session;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@javax.persistence.Entity
@Table(name="demo_entity_record")
public class DemoEntityRecord extends DatabaseEntry implements IdentifiedEntity {

    public static final String DB_FIELD_IDENTIFICATION = "identification";

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    //@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = DB_FIELD_IDENTIFICATION)
    public Identification identification;

    @Override
    public Identification getIdentification() {
        return this.identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public DemoEntityRecord() {
    }

    public DemoEntityRecord(Identification identification) {
        this.identification = identification;
    }

    public DemoEntityRecord(UUID uuid, String domain) {
        this(new Identification(uuid, domain));
    }


    public UUID getUUID() {
        return this.identification.getUuid();
    }


    public static final String DB_FIELD_ADDRESS_NUMBER = "number";
    public static final String IO_FIELD_ADDRESS_NUMBER = "nummer";

    @Column(name = DB_FIELD_ADDRESS_NUMBER)
    @JsonProperty(IO_FIELD_ADDRESS_NUMBER)
    private int postnr;

    public int getPostnr() {
        return this.postnr;
    }

    public void setPostnr(int postnr) {
        this.postnr = postnr;
    }


    public static final String DB_FIELD_ADDRESS_NAME = "name";
    public static final String IO_FIELD_ADDRESS_NAME = "navn";
    @OneToMany(mappedBy = DemoDataRecord.DB_FIELD_ENTITY, cascade = CascadeType.ALL)
    @Filters({
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTFROM_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTFROM_BEFORE),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_AFTER, condition = Bitemporal.FILTERLOGIC_EFFECTTO_AFTER),
            @Filter(name = Bitemporal.FILTER_EFFECTTO_BEFORE, condition = Bitemporal.FILTERLOGIC_EFFECTTO_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONFROM_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONFROM_BEFORE),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_AFTER, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_AFTER),
            @Filter(name = Monotemporal.FILTER_REGISTRATIONTO_BEFORE, condition = Monotemporal.FILTERLOGIC_REGISTRATIONTO_BEFORE),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_AFTER, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_AFTER),
            @Filter(name = Nontemporal.FILTER_LASTUPDATED_BEFORE, condition = Nontemporal.FILTERLOGIC_LASTUPDATED_BEFORE)
    })
    @JsonProperty(IO_FIELD_ADDRESS_NAME)
    Set<DemoDataRecord> name = new HashSet<>();

    public Set<DemoDataRecord> getName() {
        return this.name;
    }

    @Override
    public IdentifiedEntity getNewest(Collection<IdentifiedEntity> set) {
        return null;
    }

    @Override
    public void forceLoad(Session session) {

    }

    public void addBitemporalRecord(DemoDataRecord record, Session session) {
        if (record != null) {
            for (DemoDataRecord oldItem : this.name) {
                if (oldItem.equalData(record) && oldItem.getBitemporality().equals(record.getBitemporality())) {
                    // Same item, don't add
                    System.out.println("not adding");
                    return;
                }
            }
            System.out.println("adding");
            this.name.add(record);
            record.setEntity(this);
            session.saveOrUpdate(record);
        }
    }

}
