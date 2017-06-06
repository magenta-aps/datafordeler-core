package dk.magenta.datafordeler.plugindemo.fapi;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.fapi.QueryField;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 19-04-17.
 */
public class DemoQuery extends Query {

    public static final String POSTNR = "postnr";
    public static final String BYNAVN = "bynavn";
    public static final String AKTIV = "aktiv";

    public DemoQuery(){}

    @QueryField(type = QueryField.FieldType.INT)
    private String postnr;

    @QueryField(type = QueryField.FieldType.STRING)
    private String bynavn;

    @QueryField(type = QueryField.FieldType.BOOLEAN)
    private String aktiv;

    public String getPostnr() {
        return postnr;
    }

    public void setPostnr(int postnr) {
        this.postnr = Integer.toString(postnr);
    }

    public void setPostnr(String postnr) {
        this.postnr = postnr;
    }

    public String getBynavn() {
        return bynavn;
    }

    public void setBynavn(String bynavn) {
        this.bynavn = bynavn;
    }

    public String getAktiv() {
        return aktiv;
    }

    public void setAktiv(String aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("postnr", this.postnr);
        map.put("bynavn", this.bynavn);
        map.put("aktiv", this.aktiv);
        return map;
    }

    @Override
    public void setFromParameters(ListHashMap<String, String> listHashMap) {
        this.setPostnr(listHashMap.get(POSTNR, 0));
        this.setBynavn(listHashMap.get(BYNAVN, 0));
        this.setAktiv(listHashMap.get(AKTIV, 0));
    }

    @Override
    public Class<? extends Entity> getEntityClass() {
        return DemoEntity.class;
    }
}
