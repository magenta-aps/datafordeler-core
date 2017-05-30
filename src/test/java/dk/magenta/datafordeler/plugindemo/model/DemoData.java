package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DataItem;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 21-02-17.
 */
@javax.persistence.Entity
@Table(name="demo_data")
@XmlRootElement
public class DemoData extends DataItem<DemoEffect, DemoData> {

    @Column
    @JsonProperty("nummer")
    private int postnr;

    @Column
    @JsonProperty("bynavn")
    private String bynavn;

    public DemoData() {}


    public DemoData(int postnr, String bynavn) {
        this.postnr = postnr;
        this.bynavn = bynavn;
    }


    @XmlElement(name="postnr")
    public int getPostnr() {
        return postnr;
    }

    @XmlElement(name="bynavn")
    public String getBynavn() {
        return bynavn;
    }

    @Override
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("postnr", this.postnr);
        map.put("bynavn", this.bynavn);
        return map;
    }
}
