package com.mservicetech.client.mapping;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * Mapper to automatically extract details from a json.
 * Field mapping can be annotation based on config file based.
 *
 */
public interface ClientJsonMapper {

    /**
     * Create a map to hold all field mappings.
     *
     * @param serializableFieldsMappings - List of file paths for fields mappings.
     */
    void setMappingFiles(final List<String> serializableFieldsMappings);


    /**
     * Create a Class instance with values extracted from a json string.
     *
     * @param json - Json string.
     * @param clazz - Class instance type.
     * @param rootPath - Path element to check. This is null when start from the root.
     * @return Instance with values.
     */
    <T> T fromString(final String json, final Class<T> clazz, final String rootPath);

    /**
     * Create a Class instance with values extracted from a JsonObject.
     *
     * @param root - JsonObject instance.
     * @param clazz - Class instance type.
     * @param rootPath - Path element to check. This is null when start from the root.
     * @return Instance with values.
     */
    <T> T fromJsonObject(JsonObject root, final Class<T> clazz, final String rootPath);


    /**
     * Create a List of Class instances with values extracted from a json string.
     *
     * @param json - Json string.
     * @param clazz - Class instance type.
     * @param rootPath - Path element to check. This is null when start from the root.
     * @return Instance with values.
     */
    <T> List<T>  listFromString(final String json, final Class<T> clazz, final String rootPath);

    /**
     * Create a List of Class instances with values extracted from a JsonObject.
     *
     * @param rootObject - JsonObject or JsonArray instance.
     * @param clazz - Class instance type.
     * @param rootPath - Path element to check. This is null when start from the root.
     * @return Instance with values.
     */
    <T> List<T> listFromJsonObject(Object rootObject, final Class<T> clazz, final String rootPath);

    /**
     * Clear all field mappings.
     *
     */
    void clearMappingFiles();

}
