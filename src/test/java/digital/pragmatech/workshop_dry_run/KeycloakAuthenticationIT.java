package digital.pragmatech.workshop_dry_run;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import digital.pragmatech.workshop_dry_run.support.AbstractIntegrationTest;

/**
 * Verifies the <b>Keycloak (Docker)</b> part of the local setup: the resource server rejects
 * anonymous writes and accepts a real JWT minted by the Keycloak Testcontainer.
 *
 * <ul>
 *   <li>No token → 401 Unauthorized (the resource server enforces authentication).</li>
 *   <li>{@code bob} (has {@code books:write}) → 201 Created (a real Keycloak JWT is validated and authorized).</li>
 * </ul>
 */
@DisplayName("Keycloak setup check — real JWTs are issued and authorized")
class KeycloakAuthenticationIT extends AbstractIntegrationTest {

  private static final String ISBN = "978-0132350884";

  private String creationBody() {
    return """
      {
        "isbn": "%s",
        "internalName": "clean-code-shelf-a3",
        "availabilityDate": "%s"
      }
      """.formatted(ISBN, LocalDate.now().plusDays(7));
  }

  @Test
  void shouldRejectAnonymousWrite() {
    restTestClient.post()
      .uri("/api/books")
      .contentType(MediaType.APPLICATION_JSON)
      .body(creationBody())
      .exchange()
      .expectStatus().isUnauthorized();
  }

  @Test
  void shouldAllowWriteScopedUser() {
    OPEN_LIBRARY_STUBS.stubMetadata(
      ISBN, "Clean Code", "Robert C. Martin",
      "https://covers.openlibrary.org/b/id/8085499-S.jpg");

    restTestClient.post()
      .uri("/api/books")
      .header(HttpHeaders.AUTHORIZATION, bearerToken("bob", "bob"))
      .contentType(MediaType.APPLICATION_JSON)
      .body(creationBody())
      .exchange()
      .expectStatus().isCreated();
  }
}
