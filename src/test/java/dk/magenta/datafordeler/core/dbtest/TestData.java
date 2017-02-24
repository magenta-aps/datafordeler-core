package dk.magenta.datafordeler.core.dbtest;

import dk.magenta.datafordeler.core.model.DataItem;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="test_data")
public class TestData extends DataItem<TestEffect, TestData> {

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

    @Override
    public boolean equalData(TestData other) {
        return (this.postnr != other.postnr && (this.bynavn == null ? (other.bynavn == null) : this.bynavn.equals(other.bynavn)));
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("postnr", this.postnr);
        map.put("bynavn", this.bynavn);
        return map;
    }
}
