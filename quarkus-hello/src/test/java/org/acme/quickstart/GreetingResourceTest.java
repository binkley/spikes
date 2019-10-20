package org.acme.quickstart;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testHelloEndpoint() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("hello"));
    }

    @Test
    void testGreetingEndpoint() {
        // TODO: Ick, flakey test
        final String aName = randomUUID().toString();

        given()
                .pathParam("name", aName)
                .when().get("/hello/greeting/{name}", aName)
                .then()
                .statusCode(200)
                .body(is("Hello, " + aName + "!"));
    }
}
