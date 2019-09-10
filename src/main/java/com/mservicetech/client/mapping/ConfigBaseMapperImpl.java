package com.mservicetech.client.mapping;

import com.google.gson.*;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ConfigBaseMapperImpl extends  BaseMapperImpl implements ClientJsonMapper {

	/** This is used when annotations are not available to hold field details. */
	private Map<String, Map<String, Map<String, String>>> fieldMappings ;


	public ConfigBaseMapperImpl(String mappingFilesFolderName) {
		init();
		fieldMappings = new HashMap<>();
		
		if (mappingFilesFolderName == null) {
			mappingFilesFolderName = "clientFieldMappings";
        }
    	final File mappingfolder = new File(mappingFilesFolderName);
    	if (mappingfolder.exists()) {
	    	try (final Stream<Path> walk = Files.walk(Paths.get(mappingFilesFolderName))) {
	
	    		final List<String> serializableFieldsMappings = walk.filter(Files::isRegularFile)
	    				.map(x -> x.toString()).collect(Collectors.toList());
	    		this.setMappingFiles(serializableFieldsMappings);
	    	} catch (IOException e) {
	    		throw new MappingException("MappingError when loading mapping files: ", e);
	    	}
    	}
		
	}

	@Override
	public void clearMappingFiles() {
		fieldMappings.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setMappingFiles(final List<String> serializableFieldsMappings) {
		InputStream resourceAsStream = null;
		try {
			if (serializableFieldsMappings != null && !serializableFieldsMappings.isEmpty()) {
				final Yaml yaml = new Yaml();
				Map<String, Map<String, Map<String, String>>> mappings = null;
				for (String path : serializableFieldsMappings) {
					resourceAsStream = new FileInputStream(path);
					mappings = yaml.load(resourceAsStream);
					
					fieldMappings.putAll(mappings);
				}
				resourceAsStream.close();
			}
		} catch (Exception e) {
			throw new MappingException("MappingError happen on loading mapping file: ", e);
		} finally {
			try {
				if (resourceAsStream != null) {
					resourceAsStream.close();
				}
			} catch (IOException e) {
				throw new MappingException("MappingError: ", e);
			}
		}
	}

	/**
	 * Map elements from the JsonObject to the Class.
	 * Filed details are in mapping files.
	 * 
	 * @param root - JsonObject instance.
	 * @param clazz - Type to set the values.
	 * @return Instance with values.
	 */
	protected <T> T mapResult(final JsonObject root, final Class<T> clazz) {
		 T object;
		try {
			object = clazz.newInstance();
			for (Field field : clazz.getDeclaredFields()) {
		        field.setAccessible(true);
				CustomConverter customConverter = null;
				Object value = null;
				Map<String, String> fieldMap = fieldMappings.get(clazz.getSimpleName()).get(field.getName());
		        if (fieldMap == null) {
		        	continue;
		        }

				String path = fieldMap.get("scanPath");
	        	if (path == null || path.isEmpty()) {
	        		path = field.getName();
	        	}
				String  customConverterPath = fieldMap.get("customConverter");
	        	if (customConverterPath != null) {
					Class<?>  instance = Class.forName(customConverterPath);
	        		customConverter = (CustomConverter) instance.newInstance();
	        	}
	        	
	        	if (!Boolean.valueOf(String.valueOf(fieldMap.get("forceCustomConverter")))) {
			        switch (fieldMap.get("type")) {
			        	case "MappingElement":
			        		final String[] pathElements = path.split("\\,"); // In scanPath it is possible to define more than one places that is possible to extract the value.
			        		for (int i=0; i<pathElements.length; i++) {
			        			value = extractValue(root, pathElements[i], field.getType());
			        			if (value != null) {
			        				break;
			        			}
			        		}
			        		break;
			        	case "MappingObject":
			        		value = mapResult(root.getAsJsonObject(path), field.getType());
			        		break;
			        	case "MappingCollection":
			        		Class<?> listClass;
				        	if (field.getType().isArray()) {
				        		listClass = field.getType().getComponentType();
				        		value = createArray(root, path, listClass);
				        	} else if (Map.class.isAssignableFrom(field.getType())) {
				        		final ParameterizedType mapType = (ParameterizedType) field.getGenericType();
				        		value = createMap(root, path, mapType);
				        	} else {
				        		final ParameterizedType listType = (ParameterizedType) field.getGenericType();
					            listClass = (Class<?>) listType.getActualTypeArguments()[0];
					            value = createList(root, path, listClass);
				        	}
			        		break;
			        }
	        	}
		        
		        if (customConverter != null) {
	        		value = customConverter.convert(root, field, path, fieldMap.get("dependingOn"), value, object);
	        	}
		        field.set(object, value);
		    }
		} catch (Exception e) {
			throw new MappingException("MappingError: ", e);
		}
		
		return object;
	}
	


}
