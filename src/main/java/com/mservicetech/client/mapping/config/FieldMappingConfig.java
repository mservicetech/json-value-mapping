package com.mservicetech.client.mapping.config;

public class FieldMappingConfig {

    private String type;
    private String dependingOn;
    private boolean forceCustomConverter;
    private String customConverter;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDependingOn() {
        return dependingOn;
    }

    public void setDependingOn(String dependingOn) {
        this.dependingOn = dependingOn;
    }

    public boolean isForceCustomConverter() {
        return forceCustomConverter;
    }

    public void setForceCustomConverter(boolean forceCustomConverter) {
        this.forceCustomConverter = forceCustomConverter;
    }

    public String getCustomConverter() {
        return customConverter;
    }

    public void setCustomConverter(String customConverter) {
        this.customConverter = customConverter;
    }
}
