package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.DataItem;

import javax.persistence.Table;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_data")
public class TestData extends DataItem<TestIdentification, TestEntity, TestRegistration, TestEffect> {


}
