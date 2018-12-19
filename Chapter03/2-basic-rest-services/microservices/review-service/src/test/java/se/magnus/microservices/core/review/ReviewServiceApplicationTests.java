package se.magnus.microservices.core.review;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
public class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void getReviewsByProductId() {

		int productId = 1;

		client.get()
			.uri("/review?productId=" + productId)
			.accept(APPLICATION_JSON_UTF8)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].productId").isEqualTo(productId);
	}

	@Test
	public void getReviewsMissingParameter() {

		client.get()
			.uri("/review")
			.accept(APPLICATION_JSON_UTF8)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present");
	}

	@Test
	public void getReviewsInvalidParameter() {

		client.get()
			.uri("/review?productId=no-integer")
			.accept(APPLICATION_JSON_UTF8)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getReviewsNotFound() {

		int productIdNotFound = 213;

		client.get()
			.uri("/review?productId=" + productIdNotFound)
			.accept(APPLICATION_JSON_UTF8)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getReviewsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		client.get()
			.uri("/review?productId=" + productIdInvalid)
			.accept(APPLICATION_JSON_UTF8)
			.exchange()
			.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(APPLICATION_JSON_UTF8)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}
}
