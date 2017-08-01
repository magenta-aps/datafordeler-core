package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;

public class RestServiceDescriptor extends ServiceDescriptor {

    private Class<? extends Query> queryClass;

    public class RestServiceQueryField {
        public String name;
        public String type;
    }

    public RestServiceDescriptor(Plugin plugin, String serviceName, String metaAddress, Class<? extends Query> queryClass) {
        super(plugin, serviceName, metaAddress);
        this.queryClass = queryClass;
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

    @JsonProperty(value = "search_queryfields")
    public List<RestServiceQueryField> getFields() {
        ArrayList<RestServiceQueryField> fields = new ArrayList<>();
        for (Field field : getAllFields(this.queryClass)) {
            QueryField qf = field.getAnnotation(QueryField.class);
            RestServiceQueryField queryField = new RestServiceQueryField();
            queryField.name = qf.queryName();
            queryField.type = qf.type().name().toLowerCase();
            fields.add(queryField);
        }
        return fields;
    }

    private static Set<Field> getAllFields(Class queryClass) {
        HashSet<Field> fields = new HashSet<>();
        if (queryClass != null) {
            if (queryClass != Query.class) {
                fields.addAll(getAllFields(queryClass.getSuperclass()));
            }
            for (Field field : queryClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(QueryField.class)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }
}

