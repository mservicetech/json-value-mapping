package com.mservicetech.client.mapping;

import com.google.gson.*;
import com.mservicetech.client.mapping.adapter.LocalDateAdapter;
import com.mservicetech.client.mapping.adapter.LocalDateTimeAdapter;
import com.mservicetech.client.mapping.annotation.MappingSerializable;
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


public abstract  class BaseMapperImpl  implements  ClientJsonMapper{

	protected static final String PATH_CONTAINS_NULL_ELEMENTS = "The scan path contains null elements unable to map.";
	protected GsonBuilder gsonBuilder;
	protected Gson GSON = null;
	protected JsonParser parser = null;



	protected void init() {
		parser = new JsonParser();
		gsonBuilder  = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(LocalDate.class, new LocalDateAdapter().nullSafe())
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe());

	}

	protected  abstract  <T> T mapResult(final JsonObject root, final Class<T> clazz);

	@Override
	public ClientJsonMapper registerTypeAdapter(Type type, Object typeAdapter) {
		gsonBuilder.registerTypeAdapter(type, typeAdapter);
		return this;
	}

	@Override
	public <T> T  fromString(final String json, final Class<T> clazz, final String rootPath){
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
			return mapResult(root, clazz);

		} catch (Exception e) {
			throw new MappingException("MappingError: ", e);
		}
	}

	@Override
	public <T> T fromJsonObject(JsonObject root, final Class<T> clazz, final String rootPath){
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
			return mapResult(root, clazz);
		} catch (Exception e) {
			throw new MappingException("MappingError: ", e);
		}

	}

	@Override
	public  <T> List<T> listFromString(final String json, final Class<T> clazz, final String rootPath) {

		List<T> list;
		final Object root = parser.parse(json);
		list = listFromJsonObject(root, clazz, rootPath);
		return list;
	}

	@Override
	public  <T> List<T>listFromJsonObject(Object rootObject, final Class<T> clazz, final String rootPath) {
		List<T> list;
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
		T object;
		for (JsonElement element : array) {
			object = mapResult(element.getAsJsonObject(), clazz);
			list.add(object);
		}

		return list;
	}


	@Override
	public  boolean isSerializable(final Class<?> clazz) {
		if (clazz.isAnnotationPresent(MappingSerializable.class)) {
			return true;
		}
		return false;
	}

	@Override
	public ClientJsonMapper builder() {
		this.GSON = gsonBuilder.create();
		return this;
	}

	@Override
	public void clearMappingFiles() {

	}

	@Override
	@SuppressWarnings("unchecked")
	public void setMappingFiles(final List<String> serializableFieldsMappings) {

	}

	protected  <T> T extractValue(final JsonObject root, final String path, final Class<T> type) {
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
		if (GSON==null) GSON = this.gsonBuilder.create();
		return GSON.fromJson(jsonElement, type);
	}
	

	@SuppressWarnings("unchecked")
	protected <T> T[] createArray(final JsonObject root, final String path, final Class<T> listClass) {
		final JsonArray jsonArray = getArrayElement(root, path);
		final T[] array = (T[]) Array.newInstance(listClass, jsonArray.size());
		if (GSON==null) GSON = this.gsonBuilder.create();
		for (int i = 0; i < jsonArray.size(); ++i) {
			try {
				if (jsonArray.get(i) instanceof JsonPrimitive) {
					array[i] = GSON.fromJson(jsonArray.get(i), listClass);;
				} else {
					Object element = mapResult((JsonObject) jsonArray.get(i), listClass);
					array[i] = (T) element;
				}
			}  catch (Exception e) {
				throw new MappingException("MappingError: ", e);
			}
	    }
		
		return array;
	}

	protected <T> List<T> createList(final JsonObject root, final String path, final Class<T> listClass) throws MappingException{
		List<T> list = null;
		if (GSON==null) GSON = this.gsonBuilder.create();
		final JsonArray array = getArrayElement(root, path);
		if (array != null) {
			list = new ArrayList<>();
			for (JsonElement dt : array) {
				try {
					if (dt instanceof JsonPrimitive) {
						list.add(GSON.fromJson(dt, listClass));
					} else {
						T listElement =  mapResult(dt.getAsJsonObject(), listClass);
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
	protected Map<Object, Object> createMap(JsonObject root, final String path, final ParameterizedType type){
		final Map<Object, Object> map = new HashMap<>();
		if (GSON==null) GSON = this.gsonBuilder.create();
		root = getElement(root, path);
		map.putAll( GSON.fromJson(root, type));
		
		return map;
	}

	protected JsonArray getArrayElement(final JsonObject root, final String path) {
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

	protected JsonObject getElement(JsonObject root, final String path) {
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

}
