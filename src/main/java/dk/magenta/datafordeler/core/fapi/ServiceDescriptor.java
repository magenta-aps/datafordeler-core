package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.magenta.datafordeler.core.plugin.Plugin;

public abstract class ServiceDescriptor {

    private Plugin plugin;
    private String serviceName;
    private String serviceAddress;

    public ServiceDescriptor(Plugin plugin, String serviceName, String serviceAddress) {
        this.plugin = plugin;
        this.serviceName = serviceName;
        if (serviceAddress.endsWith("/")) {
            serviceAddress = serviceAddress.substring(0, serviceAddress.length() - 1);
        }
        this.serviceAddress = serviceAddress;
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

}
