package com.mservicetech.client.mapping;

@MappingSerializable
public class User {
	@MappingElement
	private String name;
	
	@MappingElement
	private String id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
