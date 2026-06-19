package digital.pragmatech.workshop_dry_run;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import digital.pragmatech.workshop_dry_run.support.AbstractIntegrationTest;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the <b>WireMock</b> part of the local setup: creating a book triggers a real HTTP call to
 * the (stubbed) OpenLibrary metadata API, and the response is used to enrich the persisted book.
 */
@DisplayName("WireMock setup check — OpenLibrary metadata is stubbed and consumed")
class OpenLibraryWireMockIT extends AbstractIntegrationTest {

  private static final String ISBN = "978-0132350884";

  @Test
  void shouldCreateBookEnrichedFromStubbedOpenLibrary() {
    OPEN_LIBRARY_STUBS.stubMetadata(
      ISBN,
      "Clean Code",
      "Robert C. Martin",
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
      .expectStatus().isCreated()
      .expectHeader().exists("Location");

    assertThat(bookRepository.findByIsbn(ISBN))
      .hasValueSatisfying(book -> {
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getThumbnailUrl())
          .isEqualTo("https://covers.openlibrary.org/b/id/8085499-S.jpg");
        assertThat(book.getInternalName()).isEqualTo("clean-code-shelf-a3");
      });

    WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/books"))
      .withQueryParam("bibkeys", equalTo(ISBN)));
  }
}
