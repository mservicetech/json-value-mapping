package com.mservicetech.client.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;

@MappingSerializable
public class TypeCustomConverter implements CustomConverter {

	@Override
	public Object convert(final JsonObject json, final Field field, final String scanPath, final String dependingOn, final Object currentValue, final Object object) throws Exception {
		final JsonElement jsonElement = json.get(dependingOn);
		Object value = currentValue;
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			value = Type.Data;
		}
		return value;
	}

}
