package com.mservicetech.client.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate the element is a type.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MappingElement {
	/** Look for this element hierarchy to extract the values. */
	 String scanPath() default "";
	
	/** The values of the element is depends on this element. To get this work you have to implement CustomConverter. */
	 String dependingOn() default "";
	
	/** Directly execute custom converter implementation. This is used when the current value is not considered. */
	 boolean forceCustomConverter() default false;
	
	/** Custom converter implementation. */
	 String customConverter() default "";
}
