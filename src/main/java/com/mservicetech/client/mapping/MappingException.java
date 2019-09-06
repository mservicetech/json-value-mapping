package com.mservicetech.client.mapping;


public class MappingException extends RuntimeException {

	private static final long serialVersionUID = 72677619351228229L;

	public MappingException(final String errorMessage) {
        super(errorMessage);
    } 
	
	public MappingException(final String errorMessage, final Throwable err) {
        super(errorMessage, err);
    }
}
