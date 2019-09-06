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

/**
 * Mapper to automatically extract details from a json.
 * Field mapping can be annotation based on config file based.
 *
 */
public class ClientJsonMapperImpl implements ClientJsonMapper {
	private static final String PATH_CONTAINS_NULL_ELEMENTS = "The scan path contains null elements unable to map.";
	private Gson GSON = null;
	private JsonParser parser = null;
	
	/** This is used when annotations are not available to hold field details. */
	private Map<String, Map<String, Map<String, String>>> fieldMappings = null;
	
	
	public ClientJsonMapperImpl() {
		init();
	}

	public ClientJsonMapperImpl(String mappingFilesFolderName) {
		init();
		
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
	
	private void init() {
		fieldMappings = new HashMap<>();
		parser = new JsonParser();
		
		GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe())
		.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
        .create();
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

	@Override
	public Object fromString(final String json, final Class<?> clazz, final String rootPath){
		Object object;
		try {
			JsonObject root = (JsonObject) parser.parse(json);
			
			if (rootPath != null) {
				final String[] pathElements = rootPath.split("\\.");
				
				for (int i = 0; i < pathElements.length; i++) {
					root = root.getAsJsonObject(pathElements[i]);
					if (root == null || root.isJsonNull()) {
						throw new MappingException(PATH_CONTAINS_NULL_ELEMENTS);
					}
				}
			}
			if (!fieldMappings.isEmpty()) {
				object = mapUsingFiles(root, clazz);
			} else {
				object = mapUsingAnnotations(root, clazz);
			}

		} catch (Exception e) {
			throw new MappingException("MappingError: ", e);
		}
		return object;
	}

	@Override
	public Object fromJsonObject(JsonObject root, final Class<?> clazz, final String rootPath){
		Object object;
		try {
			if (rootPath != null) {
				final String[] pathElements = rootPath.split("\\.");
				
				for (int i = 0; i < pathElements.length; i++) {
					root = root.getAsJsonObject(pathElements[i]);
					if (root == null || root.isJsonNull()) {
						throw new MappingException(PATH_CONTAINS_NULL_ELEMENTS);
					}
				}
			}
			
			if (!fieldMappings.isEmpty()) {
				object = mapUsingFiles(root, clazz);
			} else {
				object = mapUsingAnnotations(root, clazz);
			}
		} catch (Exception e) {
			throw new MappingException("MappingError: ", e);
		}
		
		return object;
	}

	@Override
	public List<Object> listFromString(final String json, final Class<?> clazz, final String rootPath) {
		List<Object> list;
		final Object root = parser.parse(json);
		list = listFromJsonObject(root, clazz, rootPath);
		return list;
	}

	@Override
	public List<Object> listFromJsonObject(Object rootObject, final Class<?> clazz, final String rootPath) {
		List<Object> list;
		JsonArray array = null;
		if (rootPath != null) {
			final String[] pathElements = rootPath.split("\\.");
			JsonObject root = (JsonObject) rootObject;
			for (int i = 0; i < pathElements.length; i++) {
				if (i == (pathElements.length - 1)) {
					array = root.getAsJsonArray(pathElements[i]);
					if (array == null || array.isJsonNull()) {
						throw new MappingException(PATH_CONTAINS_NULL_ELEMENTS);
					}
				} else {
					root = root.getAsJsonObject(pathElements[i]);
				}
				if (root == null || root.isJsonNull()) {
					throw new MappingException(PATH_CONTAINS_NULL_ELEMENTS);
				}
			}
		} else {
			array = (JsonArray) rootObject;
		}

		list = new ArrayList<>();
		Object object = null;
		for (JsonElement element : array) {
			if (!fieldMappings.isEmpty()) {
				object = mapUsingFiles(element.getAsJsonObject(), clazz);
			} else {
				object = mapUsingAnnotations(element.getAsJsonObject(), clazz);
			}
			list.add(object);
		}

		return list;
	}
	
	private Object extractValue(final JsonObject root, final String path, final Class<?> type) {
		final String[] pathElements = path.split("\\.");
		
		JsonObject currentRoot = root;
		JsonElement jsonElement = null;
		for (int i = 0; i < pathElements.length; i++) {
			jsonElement = currentRoot.get(pathElements[i]);
			if (jsonElement == null || jsonElement.isJsonNull()) {
				return null;
			} else if (jsonElement.isJsonObject()) {
				// Handle object recursively.
				currentRoot = (JsonObject) jsonElement;
				return extractValue(currentRoot, pathElements[i+1], type);
			}
		}
		
		return GSON.fromJson(jsonElement, type);
	}
	
	/**
	 * Map elements from the JsonObject to the Class.
	 * Filed details are in mapping files.
	 * 
	 * @param root - JsonObject instance.
	 * @param clazz - Type to set the values.
	 * @return Instance with values.
	 */
	private Object mapUsingFiles(final JsonObject root, final Class<?> clazz) {
		Object object;
		try {
			object = clazz.newInstance();
			String path;
			Map<String, String> fieldMap ;
			CustomConverter customConverter ;
			Class<?> instantation ;
			Object value ;
			String customConverterPath;
			for (Field field : clazz.getDeclaredFields()) {
		        field.setAccessible(true);
		        customConverter = null;
		        value = null;
		        fieldMap = fieldMappings.get(clazz.getSimpleName()).get(field.getName());
		        if (fieldMap == null) {
		        	continue;
		        }
		        
		        path = fieldMap.get("scanPath");
	        	if (path == null || path.isEmpty()) {
	        		path = field.getName();
	        	}
	        	
	        	customConverterPath = fieldMap.get("customConverter");
	        	if (customConverterPath != null) {
	        		instantation = Class.forName(customConverterPath);
	        		customConverter = (CustomConverter) instantation.newInstance();
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
			        		value = mapUsingFiles(root.getAsJsonObject(path), field.getType());
			        		break;
			        	case "MappingCollection":
			        		Class<?> listClass = null;
				        	if (field.getType().isArray()) {
				        		listClass = field.getType().getComponentType();
				        		value = createArray(root, path, listClass, "mapUsingFiles");
				        	} else if (Map.class.isAssignableFrom(field.getType())) {
				        		final ParameterizedType mapType = (ParameterizedType) field.getGenericType();
				        		value = createMap(root, path, mapType, "mapUsingFiles");
				        	} else {
				        		final ParameterizedType listType = (ParameterizedType) field.getGenericType();
					            listClass = (Class<?>) listType.getActualTypeArguments()[0];
					            value = createList(root, path, listClass, "mapUsingFiles");
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
	
	/**
	 * Map elements from the JsonObject to the Class.
	 * Filed details are in annotations.
	 * 
	 * @param root - JsonObject instance.
	 * @param clazz - Type to set the values.
	 * @return Instance with values.
	 */
	private Object mapUsingAnnotations(final JsonObject root, final Class<?> clazz){
		checkIfSerializable(clazz);
		
		Object object;
		try {
			object = clazz.newInstance();
			MappingElement mappingElement;
			MappingObject mappingObject;
			MappingCollection mappingCollection;
			String path = null;
			CustomConverter customConverter;
			Class<?> instantation;
			Object value;
			for (Field field : clazz.getDeclaredFields()) {
		        field.setAccessible(true);
		        value = null;
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
		        		instantation = Class.forName(mappingElement.customConverter());
		        		customConverter = (CustomConverter) instantation.newInstance();
		        		
		        		value = customConverter.convert(root, field, path, mappingElement.dependingOn(), value, object);
		        	}
		        } else if (field.isAnnotationPresent(MappingObject.class)) {
		        	mappingObject = field.getAnnotation(MappingObject.class);
		        	
		        	path = mappingObject.scanPath();
		        	if (mappingObject.scanPath().isEmpty()) {
		        		path = field.getName();
		        	}
		        	
		        	if (!mappingObject.forceCustomConverter()) {
		        		value = mapUsingAnnotations(root.getAsJsonObject(path), field.getType());
		        	}
		        	if (!mappingObject.customConverter().isEmpty()) {
		        		instantation = Class.forName(mappingObject.customConverter());
		        		customConverter = (CustomConverter) instantation.newInstance();
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
			        		value = createArray(root, path, listClass, "mapUsingAnnotations");
			        	} else if (Map.class.isAssignableFrom(field.getType())) {
			        		final ParameterizedType mapType = (ParameterizedType) field.getGenericType();
			        		value = createMap(root, path, mapType, "mapUsingAnnotations");
			        	} else {
			        		final ParameterizedType listType = (ParameterizedType) field.getGenericType();
				            listClass = (Class<?>) listType.getActualTypeArguments()[0];
				            value = createList(root, path, listClass, "mapUsingAnnotations");
			        	}
		        	}
		            
		        	if (!mappingCollection.customConverter().isEmpty()) {
		        		instantation = Class.forName(mappingCollection.customConverter());
		        		customConverter = (CustomConverter) instantation.newInstance();
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

	@SuppressWarnings("unchecked")
	private <T> T[] createArray(final JsonObject root, final String path, final Class<T> listClass, final String method) {
		final JsonArray jsonArray = getArrayElement(root, path);
		final T[] array = (T[]) Array.newInstance(listClass, jsonArray.size());
				
		for (int i = 0; i < jsonArray.size(); ++i) {
			try {
				if (jsonArray.get(i) instanceof JsonPrimitive) {
					array[i] = GSON.fromJson(jsonArray.get(i), listClass);;
				} else {
					Object element = null;
					if (method.equals("mapUsingAnnotations")) {
						element = mapUsingAnnotations((JsonObject) jsonArray.get(i), listClass);
					} else {
						element = mapUsingFiles((JsonObject) jsonArray.get(i), listClass);
					}
					array[i] = (T) element;
				}
			}  catch (Exception e) {
				throw new MappingException("MappingError: ", e);
			}
	    }
		
		return array;
	}
	
	private List<Object> createList(final JsonObject root, final String path, final Class<?> listClass, final String method) throws MappingException{
		List<Object> list = null;
		
		final JsonArray array = getArrayElement(root, path);
		if (array != null) {
			list = new ArrayList<>();
			for (JsonElement dt : array) {
				try {
					if (dt instanceof JsonPrimitive) {
						list.add(GSON.fromJson(dt, listClass));
					} else {
						Object listElement = null;
						if (method.equals("mapUsingAnnotations")) {
							listElement = mapUsingAnnotations(dt.getAsJsonObject(), listClass);
						} else {
							listElement = mapUsingFiles(dt.getAsJsonObject(), listClass);
						}
						list.add(listElement);
					}
				}  catch (Exception e) {
					throw new MappingException("MappingError: ", e);
				}
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Object, Object> createMap(JsonObject root, final String path, final ParameterizedType type, final String method){
		final Map<Object, Object> map = new HashMap<>();
		
		root = getElement(root, path);
		map.putAll( GSON.fromJson(root, type));
		
		return map;
	}
	
	private JsonArray getArrayElement(final JsonObject root, final String path) {
		final String[] pathElements = path.split("\\.");
		
		JsonArray jsonArray = null;
		JsonObject currentRoot = root;
		JsonElement element;
		for (int i = 0; i < pathElements.length; i++) {
			element = currentRoot.get(pathElements[i]);
			if (element == null || element.isJsonNull()) {
				break; // The scan path contains null elements unable to map.
			} 
			
			if (element instanceof JsonArray) {
				jsonArray = element.getAsJsonArray();
			} else {
				currentRoot = currentRoot.get(pathElements[i]).getAsJsonObject();
			}
		}
		
		return jsonArray;
	}
	
	private JsonObject getElement(JsonObject root, final String path) {
		final String[] pathElements = path.split("\\.");
		
		JsonObject currentRoot = root;
		for (int i = 0; i < pathElements.length; i++) {
			currentRoot = currentRoot.get(pathElements[i]).getAsJsonObject();
			if (currentRoot == null || currentRoot.isJsonNull()) {
				throw new MappingException(PATH_CONTAINS_NULL_ELEMENTS);
			} 
		}
		
		return currentRoot;
	}
	
	private void checkIfSerializable(final Class<?> clazz) {
	    if (!clazz.isAnnotationPresent(MappingSerializable.class)) {
	        throw new MappingException("The class "
	          + clazz.getSimpleName() 
	          + " is not annotated with MappingSerializable");
	    }
	}

}
