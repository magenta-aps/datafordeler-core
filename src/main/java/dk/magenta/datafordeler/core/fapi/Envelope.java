package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.user.DafoUserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lars on 01-07-17.
 */
public class Envelope<E extends Entity> {

    public Envelope() {
        try {
            this.terms = new URL("http://doc.data.gl/terms");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.requestTimestamp = OffsetDateTime.now();
    }

    @JsonProperty
    @XmlElement
    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty
    @XmlElement
    private URL terms;

    public void setTerms(URL terms) {
        this.terms = terms;
    }

    @JsonProperty
    @XmlElement
    private OffsetDateTime requestTimestamp;

    public void setRequestTimestamp(OffsetDateTime requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    @JsonProperty
    @XmlElement
    private OffsetDateTime responseTimestamp;

    public void setResponseTimestamp(OffsetDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @JsonProperty
    @XmlElement
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty
    @XmlElement
    private int page;

    public void setPage(int page) {
        this.page = page;
    }

    @JsonProperty
    @XmlElement
    private int pageSize;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @JsonProperty
    @XmlElement
    private List<E> results;

    public void setResults(Collection<E> results) {
        this.results = new LinkedList<>(results);
    }

    public void setResult(E result) {
        this.results = Collections.singletonList(result);
    }

    public void addQueryData(Query query) {
        this.setPage(query.getPage());
        this.setPageSize(query.getPageSize());
    }

    public void addUserData(DafoUserDetails user) {
        this.setUsername(user.toString());
    }

    public void addRequestData(HttpServletRequest request) {
        this.setPath(request.getServletPath());
    }

    public void close() {
        this.setResponseTimestamp(OffsetDateTime.now());
    }
}
