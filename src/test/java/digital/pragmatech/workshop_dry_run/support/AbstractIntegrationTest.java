package digital.pragmatech.workshop_dry_run.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import digital.pragmatech.workshop_dry_run.repository.BookRepository;

/**
 * Shared base for the workshop's setup-verification integration tests.
 *
 * <p>It boots the full set of infrastructure a workshop student needs working locally, all started
 * once per JVM and shared (via Spring's context cache) across every {@code *IT} subclass:
 * <ul>
 *   <li><b>PostgreSQL</b> via Testcontainers + {@link ServiceConnection} (datasource auto-wired).</li>
 *   <li><b>Keycloak</b> via Testcontainers — the real OIDC issuer that mints the JWTs.</li>
 *   <li><b>Mailpit</b> via Testcontainers — captures the deletion notification e-mails.</li>
 *   <li><b>WireMock</b> in-JVM — fakes the OpenLibrary metadata API.</li>
 * </ul>
 *
 * <p>If a student's Docker is not running, the three container-backed checks fail fast with a clear
 * Docker error; if WireMock can't bind a port, only that check fails. Each test therefore points at
 * exactly one piece of the required setup.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public abstract class AbstractIntegrationTest {

  @ServiceConnection
  protected static final PostgreSQLContainer POSTGRES =
    new PostgreSQLContainer("postgres:16-alpine");

  protected static final KeycloakContainer KEYCLOAK = new KeycloakContainer();

  protected static final MailpitContainer MAILPIT = new MailpitContainer();

  protected static final WireMockServer WIREMOCK =
    new WireMockServer(WireMockConfiguration.options().dynamicPort());

  protected static final OpenLibraryStubs OPEN_LIBRARY_STUBS = new OpenLibraryStubs(WIREMOCK);

  static {
    POSTGRES.start();
    KEYCLOAK.start();
    MAILPIT.start();
    WIREMOCK.start();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("book.metadata.api.url", WIREMOCK::baseUrl);
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KEYCLOAK::getIssuerUri);
    registry.add("spring.mail.host", MAILPIT::getHost);
    registry.add("spring.mail.port", MAILPIT::getSmtpPort);
  }

  @Autowired
  protected RestTestClient restTestClient;

  @Autowired
  protected BookRepository bookRepository;

  /**
   * RANDOM_PORT commits are real (the controller runs on its own thread/transaction), so we clean up
   * explicitly to keep tests independent: clear the DB, reset WireMock stubs, and purge Mailpit.
   */
  @AfterEach
  void cleanupSharedState() {
    bookRepository.deleteAll();
    WIREMOCK.resetAll();
    purgeMailpit();
  }

  /** Mint a real Keycloak-signed access token for a realm user (e.g. {@code bob}/{@code bob}). */
  protected String bearerToken(String username, String password) {
    return "Bearer " + KEYCLOAK.getAccessToken(username, password);
  }

  private void purgeMailpit() {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(MAILPIT.getHttpBaseUrl() + "/api/v1/messages"))
      .DELETE()
      .build();
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }
    catch (InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
    }
    catch (java.io.IOException ignored) {
      // best-effort cleanup
    }
  }
}
