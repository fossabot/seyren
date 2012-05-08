package com.seyren.acceptancetests.checks;

import static com.github.restdriver.serverdriver.Matchers.*;
import static com.github.restdriver.serverdriver.RestServerDriver.*;
import static com.seyren.acceptancetests.util.SeyrenDriver.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.restdriver.serverdriver.http.response.Response;

import de.flapdoodle.embedmongo.MongoDBRuntime;
import de.flapdoodle.embedmongo.MongodExecutable;
import de.flapdoodle.embedmongo.config.MongodConfig;
import de.flapdoodle.embedmongo.distribution.Version;

public class ChecksAT {
	
	static MongodExecutable mongod;
	
	@BeforeClass
	public static void setup() throws Exception {
		MongodConfig mongodConfig = new MongodConfig(Version.V2_0, 27017, false);
		mongod = MongoDBRuntime.getDefaultInstance().prepare(mongodConfig);
		mongod.start();
	}
	
	@AfterClass
	public static void tearDown() {
		mongod.cleanup();
	}
	
	@Test
	public void testGetChecksReturnsOk() {
		Response response = get(checks());
		assertThat(response, hasStatusCode(200));
		assertThat(response.asJson(), hasJsonPath("$.", hasSize(0)));
	}
	
	@Test
	public void testGetChecksReturnsResultsOk() {
        Response createResponse = createCheck("{ }");
		Response response = get(checks());
		assertThat(response, hasStatusCode(200));
		assertThat(response.asJson(), hasJsonPath("$.", hasSize(1)));
        deleteLocation(createResponse.getHeader("Location").getValue());
	}
	
	@Test
	public void testGetChecksByErrorStateReturnsOk() {
        Response createResponse = createCheck("{ \"state\" : \"ERROR\" }");
		Response response = get(checks().withParam("states", "error"));
		assertThat(response, hasStatusCode(200));
		assertThat(response.asJson(), hasJsonPath("$.", hasSize(1)));
        deleteLocation(createResponse.getHeader("Location").getValue());
	}
	
	@Test
	public void testGetChecksByWarnStateReturnsOk() {
        Response createResponse = createCheck("{ \"state\" : \"WARN\" }");
		Response response = get(checks().withParam("states", "warn"));
		assertThat(response, hasStatusCode(200));
		assertThat(response.asJson(), hasJsonPath("$.", hasSize(1)));
        deleteLocation(createResponse.getHeader("Location").getValue());
	}
	
    @Test
    public void testCreateCheckReturnsCreated() {
        Response response = createCheck("{ }");
        deleteLocation(response.getHeader("Location").getValue());
	}
    
    private Response createCheck(String body) { 
    	Response response = post(checks(), body(body, "application/json"));
    	assertThat(response, hasStatusCode(201));
    	assertThat(response, hasHeader("Location"));
    	return response;
    }
	
	private void deleteLocation(String location) {
		assertThat(get(location), hasStatusCode(200));
        delete(location);
        assertThat(get(location), hasStatusCode(404));
	}

}