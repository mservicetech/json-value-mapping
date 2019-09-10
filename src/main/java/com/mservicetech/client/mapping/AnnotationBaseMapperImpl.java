package com.mservicetech.client.mapping;

import com.google.gson.*;
import com.mservicetech.client.mapping.adapter.LocalDateAdapter;
import com.mservicetech.client.mapping.adapter.LocalDateTimeAdapter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class AnnotationBaseMapperImpl extends  BaseMapperImpl implements ClientJsonMapper {



	public AnnotationBaseMapperImpl() {
		init();
	}

	
	/**
	 * Map elements from the JsonObject to the Class.
	 * Filed details are in annotations.
	 * 
	 * @param root - JsonObject instance.
	 * @param clazz - Type to set the values.
	 * @return Instance with values.
	 */
	protected  <T> T mapResult(final JsonObject root, final Class<T> clazz){
		if (!isSerializable(clazz) ) {
			throw new MappingException("The class " + clazz.getSimpleName() + " is not annotated with MappingSerializable");
		}
		T object;
		try {
			object = clazz.newInstance();
			MappingElement mappingElement;
			MappingObject mappingObject;
			MappingCollection mappingCollection;
			String path ;
			CustomConverter customConverter;
			Class<?> instance;
		//	Object value;
			for (Field field : clazz.getDeclaredFields()) {
		        field.setAccessible(true);
				Object value = null;
		        if (field.isAnnotationPresent(MappingElement.class)) {
		        	mappingElement = field.getAnnotation(MappingElement.class);

		        	path = mappingElement.scanPath();
		        	if (mappingElement.scanPath().isEmpty()) {
		        		path = field.getName();
		        	}
		        	
		        	if (!mappingElement.forceCustomConverter()) {
		        		final String[] pathElements = path.split("\\,"); // In scanPath it is possible to define more than one places that is possible to extract the value.
		        		for (int i=0; i<pathElements.length; i++) {
		        			value = extractValue(root, pathElements[i], field.getType());
		        			if (value != null) {
		        				break;
		        			}
		        		}
		        	}
		        	if (!mappingElement.customConverter().isEmpty()) {
						instance = Class.forName(mappingElement.customConverter());
		        		customConverter = (CustomConverter) instance.newInstance();
		        		
		        		value = customConverter.convert(root, field, path, mappingElement.dependingOn(), value, object);
		        	}
		        } else if (field.isAnnotationPresent(MappingObject.class)) {
		        	mappingObject = field.getAnnotation(MappingObject.class);
		        	
		        	path = mappingObject.scanPath();
		        	if (mappingObject.scanPath().isEmpty()) {
		        		path = field.getName();
		        	}
		        	
		        	if (!mappingObject.forceCustomConverter()) {
		        		value = mapResult(root.getAsJsonObject(path), field.getType());
		        	}
		        	if (!mappingObject.customConverter().isEmpty()) {
						instance = Class.forName(mappingObject.customConverter());
		        		customConverter = (CustomConverter) instance.newInstance();
		        		value = customConverter.convert(root, field, path, mappingObject.dependingOn(), value, object);
		        	}
		        } else if (field.isAnnotationPresent(MappingCollection.class)) {
		        	mappingCollection = field.getAnnotation(MappingCollection.class);
		        	
		        	path = mappingCollection.scanPath();
		        	if (mappingCollection.scanPath().isEmpty()) {
		        		path = field.getName();
		        	}
		        	
		        	if (!mappingCollection.forceCustomConverter()) {
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
		        	}
		            
		        	if (!mappingCollection.customConverter().isEmpty()) {
						instance = Class.forName(mappingCollection.customConverter());
		        		customConverter = (CustomConverter) instance.newInstance();
		        		value = customConverter.convert(root, field, path, mappingCollection.dependingOn(), value, object);
		        	}
		        }
		        field.set(object, value);
		    }
		} catch (Exception e) {
			throw new MappingException("MappingError: ", e);
		}
		
		return object;
	}

}
