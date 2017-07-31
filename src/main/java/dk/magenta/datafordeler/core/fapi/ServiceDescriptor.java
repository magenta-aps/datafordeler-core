package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ServiceDescriptor {

    private String serviceName;
    private String serviceAddress;

    public ServiceDescriptor(String serviceName, String serviceAddress) {
        this.serviceName = serviceName;
        if (serviceAddress.endsWith("/")) {
            serviceAddress = serviceAddress.substring(0, serviceAddress.length() - 1);
        }
        this.serviceAddress = serviceAddress;
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
    protected String getServiceAddress() {
        return this.serviceAddress;
    }

    @JsonProperty(value = "type")
    public abstract String getType();

    public String toHTML(boolean includeServiceName) {
        StringBuilder sb = new StringBuilder();
        if (includeServiceName) {
            sb.append("<h1>" + this.serviceName + "</h1>");
        }
        sb.append("<h2>" + this.serviceAddress + "</h2>");
        return sb.toString();
    }

}
