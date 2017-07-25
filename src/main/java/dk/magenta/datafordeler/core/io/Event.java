package dk.magenta.datafordeler.core.io;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * Created by lars on 15-02-17.
 *
 * A generic envelope for an io, containing the payload in parseable strings
 */
public class Event implements PluginSourceData {

    public class MessageData implements Serializable {

        @JsonProperty(value="Objektdata", required=false)
        private ObjectData objectData;

        @JsonProperty(value="Objektreference", required=false)
        private ObjectReference objectReference;

        @JsonIgnore
        public String getDataskema() {
            if (this.objectData != null) {
                return this.objectData.getDataskema();
            }
            return null;
        }

        @JsonIgnore
        public void setDataskema(String dataskema) {
            if (this.objectData == null) {
                this.objectData = new ObjectData();
            }
            this.objectData.setDataskema(dataskema);
        }

        @JsonIgnore
        public String getObjektData() {
            if (this.objectData != null) {
                return this.objectData.getObjektData();
            }
            return null;
        }

        @JsonIgnore
        public void setObjektData(String objektData) {
            if (this.objectData == null) {
                this.objectData = new ObjectData();
            }
            this.objectData.setObjektData(objektData);
        }

        @JsonIgnore
        public String getObjektReference() {
            if (this.objectReference != null) {
                return this.objectReference.getObjektReference();
            }
            return null;
        }

        @JsonIgnore
        public void setObjektReference(String objektReference) {
            if (this.objectReference == null) {
                this.objectReference = new ObjectReference();
            }
            this.objectReference.setObjektReference(objektReference);
        }
    }

    public static class ObjectData implements Serializable {

        @JsonProperty(value="dataskema", required=true)
        private String dataskema;

        @JsonProperty(value="objectdata", required=true)
        private String objektData;

        public ObjectData() {
        }

        public String getDataskema() {
            return dataskema;
        }

        public void setDataskema(String dataskema) {
            this.dataskema = dataskema;
        }

        public String getObjektData() {
            return objektData;
        }

        public void setObjektData(String objektData) {
            this.objektData = objektData;
        }
    }

    public static class ObjectReference implements Serializable {
        private String objektReference;

        public ObjectReference() {
        }

        @JsonProperty(value="objektreference", required=true)
        public String getObjektReference() {
            return objektReference;
        }

        @JsonProperty(value="objektreference", required=true)
        public void setObjektReference(String objektReference) {
            this.objektReference = objektReference;
        }
    }

    private String eventID;
    private String beskedVersion;

    @JsonProperty
    private MessageData beskedData;


    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getBeskedVersion() {
        return beskedVersion;
    }

    public void setBeskedVersion(String beskedVersion) {
        this.beskedVersion = beskedVersion;
    }

    private MessageData ensureMessageData() {
        if (this.beskedData == null) {
            this.beskedData = new MessageData();
        }
        return this.beskedData;
    }

    public String getSchema() {
        if (this.beskedData != null) {
            return this.beskedData.getDataskema();
        }
        return null;
    }

    @JsonProperty(required=false, value = "dataskema")
    public void setSchema(String dataskema) {
        this.ensureMessageData().setDataskema(dataskema);
    }

    public String getData() {
        if (this.beskedData != null) {
            return this.beskedData.getObjektData();
        }
        return null;
    }

    public void setData(String objektData) {
        this.ensureMessageData().setObjektData(objektData);
    }

    public String getObjektReference() {
        if (this.beskedData != null) {
            return this.beskedData.getObjektReference();
        }
        return null;
    }

    public void setObjektReference(String objektReference) {
        this.ensureMessageData().setObjektReference(objektReference);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Event(");
        StringJoiner joiner = new StringJoiner(", ");
        joiner.add("eventID: "+this.getEventID());
        joiner.add("beskedVersion: "+this.getBeskedVersion());
        joiner.add("dataskema: "+this.getSchema());
        joiner.add("objektData: "+this.getData());
        joiner.add("objektReference: "+this.getObjektReference());
        sb.append(joiner.toString());
        sb.append(")");
        return sb.toString();
    }
}
