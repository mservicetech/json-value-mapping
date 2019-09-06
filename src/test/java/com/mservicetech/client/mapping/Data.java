package com.mservicetech.client.mapping;

@MappingSerializable
public class Data {

	@MappingElement
	private int age;

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

}
