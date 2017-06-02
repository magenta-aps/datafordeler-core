package dk.magenta.datafordeler.plugindemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.DataItem;
import dk.magenta.datafordeler.core.database.Identification;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
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

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Identification reference;

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
        map.put("reference", this.reference);
        return map;
    }

    public HashMap<String, Identification> getReferences() {
        HashMap<String, Identification> references = super.getReferences();
        references.put("reference", this.reference);
        return references;
    }

    public void updateReferences(HashMap<String, Identification> references) {
        super.updateReferences(references);
        if (references.containsKey("reference")) {
            this.reference = references.get("reference");
        }
    }

    public Identification getReference() {
        return reference;
    }

    public void setReference(Identification reference) {
        this.reference = reference;
    }
}
