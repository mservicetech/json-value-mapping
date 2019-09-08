package com.mservicetech.client.mapping;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ClientJsonMapperMappingFilesTest {
	private ClientJsonMapper clientJsonMapperImpl = new ClientJsonMapperImpl("src/test/resources/clientFieldMappings");
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
	
	private final static String usersJson = "[{" +
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
        "    ]";
        
	@Test
	public void mappingFilesTest() throws Exception {
		Model value = (Model) clientJsonMapperImpl.fromString(json, Model.class, null);
	    assertEquals(value.getAge(), 30);
	    assertTrue(value.getType() == Type.Data);
	    assertTrue(value.getInfo().size() > 0);
	}

	@Test
	public void fromJsonObjectTest() throws Exception {
		final JsonParser parser = new JsonParser();
		final JsonObject root = (JsonObject) parser.parse(json);

		Model value = (Model) clientJsonMapperImpl.fromJsonObject(root, Model.class, null);
	    assertEquals(value.getAge(), 30);
	    assertTrue(value.getUsers().size() != 0);
	    assertTrue(value.getIntList().size() != 0);

	    Data data = (Data) clientJsonMapperImpl.fromJsonObject(root, Data.class, "dataInfo.info.data");
	    assertEquals(data.getAge(), 30);
	}

	@Test
	public void listFromStringTest() throws Exception {
		List<?> users =  clientJsonMapperImpl.listFromString(json, User.class, "f4");
	    assertTrue(users.size() > 0);
	    
	    users = clientJsonMapperImpl.listFromString(usersJson, User.class, null);
	    assertTrue(users.size() > 0);
	}
	
	@Test
	public void loadMappingFilesError() throws Exception {
		try {
			final ClientJsonMapperImpl clientJsonMapperImpl = new ClientJsonMapperImpl("src/test/resources/clientFieldMappingsError");
			fail();
		} catch (MappingException e) {
			assertNotNull(e);
		}
	}
}
