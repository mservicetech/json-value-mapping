package com.mservicetech.client.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@MappingSerializable
public class MyModel {
	
	@MappingElement(dependingOn = "data", customConverter = "com.mservicetech.client.mapping.TypeCustomConverter")
	private Type type;

	@JsonProperty("volumn")
	@MappingElement(scanPath = "f1")
	private String volume;

	@MappingElement(scanPath = "openDate")
    private LocalDate openDate;

	@MappingElement(scanPath = "age,data.age")
	private int age;

	@MappingObject(scanPath = "data")
	private Data data;

	@MappingCollection(scanPath = "f4")
	private List<User> users;

	@MappingCollection(scanPath = "strList")
	private List<String> strList;

	@MappingCollection(scanPath = "intList")
	private List<Integer> intList;

	@MappingCollection(scanPath = "intList")
	private Integer[] intArray;

	@MappingCollection(scanPath = "f4")
	private User[] userArray;

	@MappingCollection(scanPath = "data")
	private Map<String, String> userMap;

	@MappingCollection(scanPath = "dataInfo.info")
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

	public Integer[] getIntArray() {
		return intArray;
	}

	public void setIntArray(Integer[] intArray) {
		this.intArray = intArray;
	}

	public User[] getUserArray() {
		return userArray;
	}

	public void setUserArray(User[] userArray) {
		this.userArray = userArray;
	}

	public Map<String, String> getUserMap() {
		return userMap;
	}

	public void setUserMap(Map<String, String> userMap) {
		this.userMap = userMap;
	}

	public Map<String, Data> getInfo() {
		return info;
	}

	public void setInfo(Map<String, Data> info) {
		this.info = info;
	}
	
}
