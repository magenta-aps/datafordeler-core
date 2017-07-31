package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SoapServiceDescriptor extends ServiceDescriptor {

    public SoapServiceDescriptor(String serviceName, String metaAddress) {
        super(serviceName, metaAddress);
    }

    @JsonProperty(value = "wsdl_url")
    public String getWsdlAddress() {
        return this.getServiceAddress() + "?wsdl";
    }

    @Override
    @JsonProperty(value = "type")
    public String getType() {
        return "soap";
    }

    @Override
    public String toHTML(boolean includeServiceName) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toHTML(includeServiceName));
        sb.append("<dl class=\"dl-horizontal\">");
        sb.append("<dt>" + "Type:" + "</dt>");
        sb.append("<dd>" + this.getType() + "</dd>");
        sb.append("<dt>" + "Wsdl Address:" + "</dt>");
        sb.append("<dd>" + this.link(this.getWsdlAddress()) + "</dd>");
        sb.append("</dl>");
        return sb.toString();
    }
}
