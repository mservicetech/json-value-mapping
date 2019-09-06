package com.mservicetech.client.mapping;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClientValueJsonMapperTest {
	private ClientJsonMapper arkJsonMapper = new ClientJsonMapper();
	private final static String json = "{" +
            "    \"f1\" : \"volume\"," +
            "    \"f2\" : \"gender\"," +
            "    \"f3\" : \"days\"," +
            "    \"openDate\" : \"2019-03-14\"," +
            "    \"data\" : {\"age\" : \"30\"}," +
            "    \"dataInfo\" : {\"info\" : {\"data\" : {\"age\" : \"30\"}}}," +
            "    \"strList\" : [\"one\", \"two\"]," +
            "    \"intList\" : [1, 2]," +
            "    \"f4\" : [{" +
            "            \"id\" : \"F\"," +
            "            \"name\" : \"female\"," +
            "            \"values\" : [{" +
            "                    \"name\" : \"September\"," +
            "                    \"value\" : 12" +
            "                }" +
            "            ]" +
            "        }, {" +
            "            \"id\" : \"M\"," +
            "            \"name\" : \"male\"," +
            "            \"values\" : [{" +
            "                    \"name\" : \"September\"," +
            "                    \"value\" : 11" +
            "                }" +
            "            ]" +
            "        }" +
            "    ]" +
            "}";
	
	@Test
	public void fromStringTest() throws Exception {
		MyModel value = (MyModel) arkJsonMapper.fromString(json, MyModel.class, null);
	    assertEquals(value.getAge(), 30);
	    assertTrue(value.getIntArray().length != 0);
	    assertTrue(value.getUserArray().length != 0);
	    assertTrue(value.getInfo().size() != 0);
	    assertTrue(value.getType() == Type.Data);
	    
	    Data data = (Data) arkJsonMapper.fromString(json, Data.class, "dataInfo.info.data");
	    assertEquals(data.getAge(), 30);
	}
	
	@Test
	public void fromJsonObjectTest() throws Exception {
		final JsonParser parser = new JsonParser();
		final JsonObject root = (JsonObject) parser.parse(json);
		
		MyModel value = (MyModel) arkJsonMapper.fromJsonObject(root, MyModel.class, null);
	    assertEquals(value.getAge(), 30);
	    assertTrue(value.getIntArray().length != 0);
	    assertTrue(value.getUserArray().length != 0);
	    assertTrue(value.getInfo().size() != 0);
	    
	    Data data = (Data) arkJsonMapper.fromJsonObject(root, Data.class, "dataInfo.info.data");
	    assertEquals(data.getAge(), 30);
	}

	@Test
	public void fromStringErrorTest() throws Exception {
		try {
			arkJsonMapper.fromString(json, Data.class, "dataInfo.info1");
			fail();
		} catch (MappingException e) {

			assertNotNull(e);
		}
	}
	
	@Test
	public void fromJsonObjectErrorTest() throws Exception {
		try {
			final JsonParser parser = new JsonParser();
			final JsonObject root = (JsonObject) parser.parse(json);
			arkJsonMapper.fromJsonObject(root, Data.class, "dataInfo.info1");
			fail();
		} catch (MappingException e) {
			assertNotNull(e);
		}
	}
	
}
