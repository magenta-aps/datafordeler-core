package dk.magenta.datafordeler.plugindemo.fapi;

import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.fapi.Query;
import dk.magenta.datafordeler.core.util.ListHashMap;
import dk.magenta.datafordeler.plugindemo.model.DemoEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lars on 19-04-17.
 */
public class DemoQuery extends Query {

    public static final String POSTNR = "postnr";

    public DemoQuery(){}

    private int postnr;

    public int getPostnr() {
        return postnr;
    }

    public void setPostnr(int postnr) {
        this.postnr = postnr;
    }

    public void setPostnr(String postnr) {
        if (postnr != null) {
            this.setPostnr(Integer.parseInt(postnr));
        }
    }

    @Override
    public Map<String, Object> getSearchParameters() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("postnr", this.postnr);
        return map;
    }

    @Override
    public void setFromParameters(ListHashMap<String, String> listHashMap) {
        this.setPostnr(listHashMap.get(POSTNR, 0));
    }

    @Override
    public Class<? extends Entity> getEntityClass() {
        return DemoEntity.class;
    }
}
