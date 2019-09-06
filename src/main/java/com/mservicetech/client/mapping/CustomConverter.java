package com.mservicetech.client.mapping;

import com.google.gson.JsonObject;

import java.lang.reflect.Field;

/**
 * This is used to provide special convert function and rules  in the mapping process.
 *
 * The convert method will be called when the mapping happen
 */
public interface CustomConverter {
	/**
	 * Convert to the desired value.
	 * 
	 * @param json - Json object.
	 * @param field - Field element.
	 * @param scanPath - Path to the main sub element.
	 * @param dependingOn - Depending element.
	 * @param currentValue - Value.
	 * @param object - Main object.
	 * @return Converted value.
	 */
	public Object convert(final JsonObject json, final Field field, final String scanPath, final String dependingOn, final Object currentValue, final Object object) throws Exception;
}
