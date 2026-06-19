package digital.pragmatech.workshop_dry_run.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Test helper that turns a {@link WireMockServer} into a fake OpenLibrary
 * Books API so integration tests can avoid hand-rolling WireMock stubs in
 * every test method.
 *
 * <p>The helper keeps WireMock's fluent DSL out of the test body and gives
 * tests a small, intention-revealing API to declare which ISBNs should be
 * resolvable.
 */
public class OpenLibraryStubs {

  private final WireMockServer server;

  public OpenLibraryStubs(WireMockServer server) {
    this.server = server;
  }

  /**
   * Register a successful OpenLibrary lookup for the given ISBN. The stub
   * matches on the {@code bibkeys} query parameter so the same base URL
   * can serve different ISBNs independently.
   */
  public void stubMetadata(String isbn, String title, String author, String thumbnailUrl) {
    String body = """
      {
        "%s": {
          "title": "%s",
          "authors": [ { "name": "%s" } ],
          "cover": { "small": "%s" }
        }
      }
      """.formatted(isbn, title, author, thumbnailUrl);

    server.stubFor(get(urlPathEqualTo("/api/books"))
      .withQueryParam("bibkeys", equalTo(isbn))
      .withQueryParam("jscmd", equalTo("data"))
      .withQueryParam("format", equalTo("json"))
      .willReturn(ok()
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withBody(body)));
  }

  /**
   * Register the given ISBN as "unknown" — the stub returns an empty JSON
   * object, which {@code OpenLibraryApiClient} interprets as "no record",
   * causing the service layer to throw {@code BookMetadataUnavailableException}.
   */
  public void stubUnknown(String isbn) {
    server.stubFor(get(urlPathEqualTo("/api/books"))
      .withQueryParam("bibkeys", equalTo(isbn))
      .willReturn(ok()
        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .withBody("{}")));
  }

  /**
   * Clear every stub currently registered on the underlying WireMock server.
   * Call from {@code @BeforeEach}/{@code @AfterEach} to keep tests isolated.
   */
  public void reset() {
    server.resetAll();
  }
}
