package dk.magenta.datafordeler.core.fapi;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by lars on 19-04-17.
 */
public class Query {

    protected int page = 0;
    protected int pageSize = 10;

    public Query() {
    }

    public Query(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public Query(String page, String pageSize) {
        this(intFromString(page, 0), intFromString(pageSize, 10));
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public static int intFromString(String s, int def) {
        if (s == null) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
