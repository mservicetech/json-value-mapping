package com.mservicetech.client.mapping;

import com.mservicetech.client.mapping.annotation.MappingElement;
import com.mservicetech.client.mapping.annotation.MappingSerializable;

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
