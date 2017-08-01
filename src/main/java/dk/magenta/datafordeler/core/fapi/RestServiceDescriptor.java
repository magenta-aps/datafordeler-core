package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;

public class RestServiceDescriptor extends ServiceDescriptor {



    public RestServiceDescriptor(Plugin plugin, String serviceName, String metaAddress, Class<? extends Query> queryClass) {
        super(plugin, serviceName, metaAddress, queryClass);
    }

    @Override
    @JsonProperty(value = "type")
    public String getType() {
        return "rest";
    }

    @JsonProperty(value = "fetch_url")
    public String getFetchAddress() {
        return this.getServiceAddress() + "/{UUID}";
    }

    @JsonProperty(value = "search_url")
    public String getSearchAddress() {
        return this.getServiceAddress() + "/search";
    }

    @JsonProperty(value = "declaration_url")
    public String getDeclarationAddress() {
        return "https://redmine.magenta-aps.dk/projects/dafodoc/wiki/API";
    }




}

