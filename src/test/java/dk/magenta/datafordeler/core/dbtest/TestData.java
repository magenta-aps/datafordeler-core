package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.DataItem;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_data")
public class TestData extends DataItem<TestEffect> {

    @Column
    private int postnr;

    @Column
    private String bynavn;

    public TestData() {}

    public TestData(int postnr, String bynavn) {
        this.postnr = postnr;
        this.bynavn = bynavn;
    }

    public int getPostnr() {
        return postnr;
    }

    public String getBynavn() {
        return bynavn;
    }

}
