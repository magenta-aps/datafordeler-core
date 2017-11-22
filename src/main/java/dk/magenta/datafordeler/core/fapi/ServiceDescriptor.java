package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ServiceDescriptor {

    public class ServiceQueryField {
        public String name;
        public String type;
    }

    private Plugin plugin;
    private String serviceName;
    private String serviceAddress;
    private Class<? extends Query> queryClass;


    public ServiceDescriptor(Plugin plugin, String serviceName, String serviceAddress, Class<? extends Query> queryClass) {
        this.plugin = plugin;
        this.serviceName = serviceName;
        if (serviceAddress.endsWith("/")) {
            serviceAddress = serviceAddress.substring(0, serviceAddress.length() - 1);
        }
        this.serviceAddress = serviceAddress;
        this.queryClass = queryClass;
    }

    @JsonIgnore
    public Plugin getPlugin() {
        return this.plugin;
    }

    @JsonProperty(value = "service_name")
    public String getServiceName() {
        return this.serviceName;
    }

    @JsonProperty(value = "metadata_url")
    public String getMetaAddress() {
        return this.serviceAddress;
    }

    @JsonIgnore
    public String getServiceAddress() {
        return this.serviceAddress;
    }

    @JsonProperty(value = "type")
    public abstract String getType();

    @JsonProperty(value = "search_queryfields")
    public List<ServiceQueryField> getFields() {
        ArrayList<ServiceQueryField> fields = new ArrayList<>();
        for (Field field : getAllFields(this.queryClass)) {
            QueryField qf = field.getAnnotation(QueryField.class);
            ServiceQueryField queryField = new ServiceQueryField();
            queryField.name = qf.queryName();
            queryField.type = qf.type().name().toLowerCase();
            fields.add(queryField);
        }
        return fields;
    }

    protected static Set<Field> getAllFields(Class queryClass) {
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
