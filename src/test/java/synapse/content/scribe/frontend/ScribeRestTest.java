package synapse.content.scribe.frontend;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ScribeRestTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/rest/content/scribe")
          .then()
             .statusCode(200)
             .body(is("Hello from RESTEasy Reactive"));
    }

}