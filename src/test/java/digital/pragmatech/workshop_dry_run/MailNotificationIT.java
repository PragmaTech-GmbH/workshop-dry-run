package digital.pragmatech.workshop_dry_run;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import digital.pragmatech.workshop_dry_run.support.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Verifies the <b>Mailpit (Docker)</b> part of the local setup: deleting a book sends a notification
 * e-mail over SMTP to the Mailpit Testcontainer, which we then read back through its REST API.
 */
@DisplayName("Mailpit setup check — deletion sends an e-mail that Mailpit captures")
class MailNotificationIT extends AbstractIntegrationTest {

  private static final String ISBN = "978-0132350884";

  @Test
  void shouldSendDeletionNotificationEmail() {
    OPEN_LIBRARY_STUBS.stubMetadata(
      ISBN, "Clean Code", "Robert C. Martin",
      "https://covers.openlibrary.org/b/id/8085499-S.jpg");

    String body = """
      {
        "isbn": "%s",
        "internalName": "clean-code-shelf-a3",
        "availabilityDate": "%s"
      }
      """.formatted(ISBN, LocalDate.now().plusDays(7));

    restTestClient.post()
      .uri("/api/books")
      .header(HttpHeaders.AUTHORIZATION, bearerToken("bob", "bob"))
      .contentType(MediaType.APPLICATION_JSON)
      .body(body)
      .exchange()
      .expectStatus().isCreated();

    Long bookId = bookRepository.findByIsbn(ISBN).orElseThrow().getId();

    restTestClient.delete()
      .uri("/api/books/{id}", bookId)
      .header(HttpHeaders.AUTHORIZATION, bearerToken("bob", "bob"))
      .exchange()
      .expectStatus().isNoContent();

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
      assertThat(fetchMailpitMessages()).contains("Book removed from library"));
  }

  private String fetchMailpitMessages() throws Exception {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(MAILPIT.getHttpBaseUrl() + "/api/v1/messages"))
      .header("Accept", "application/json")
      .GET()
      .build();
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();
    }
  }
}
