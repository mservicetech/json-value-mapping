package com.mservicetech.client.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mservicetech.client.mapping.annotation.MappingSerializable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@MappingSerializable
public class Model {
	private Type type;

	@JsonProperty("volumn")
	private String volume;

    private LocalDate openDate;

	private int age;

	private Data data;

	private List<User> users;

	private List<String> strList;

	private List<Integer> intList;

	private Map<String, Data> info;


	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public LocalDate getOpenDate() {
		return openDate;
	}

	public void setOpenDate(LocalDate openDate) {
		this.openDate = openDate;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public List<String> getStrList() {
		return strList;
	}

	public void setStrList(List<String> strList) {
		this.strList = strList;
	}

	public List<Integer> getIntList() {
		return intList;
	}

	public void setIntList(List<Integer> intList) {
		this.intList = intList;
	}

	public Map<String, Data> getInfo() {
		return info;
	}

	public void setInfo(Map<String, Data> info) {
		this.info = info;
	}
	
}
