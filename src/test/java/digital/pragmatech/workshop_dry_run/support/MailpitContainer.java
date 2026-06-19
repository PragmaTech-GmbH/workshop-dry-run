package digital.pragmatech.workshop_dry_run.support;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainer for <a href="https://github.com/axllent/mailpit">Mailpit</a>, a tiny SMTP server
 * with a REST API. The application sends deletion notification e-mails over SMTP (port 1025); tests
 * read them back through the HTTP API (port 8025) to assert delivery.
 *
 * <pre>{@code
 * static final MailpitContainer MAILPIT = new MailpitContainer();
 * // SMTP:  MAILPIT.getHost() : MAILPIT.getSmtpPort()
 * // HTTP:  MAILPIT.getHttpBaseUrl() + "/api/v1/messages"
 * }</pre>
 */
public class MailpitContainer extends GenericContainer<MailpitContainer> {

  public static final int SMTP_PORT = 1025;

  public static final int HTTP_PORT = 8025;

  private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("axllent/mailpit:v1.20");

  public MailpitContainer() {
    this(DEFAULT_IMAGE);
  }

  public MailpitContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
    addExposedPorts(SMTP_PORT, HTTP_PORT);
    addEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1");
    addEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1");
    setWaitStrategy(Wait.forHttp("/readyz").forPort(HTTP_PORT));
  }

  public int getSmtpPort() {
    return getMappedPort(SMTP_PORT);
  }

  public int getHttpPort() {
    return getMappedPort(HTTP_PORT);
  }

  public String getHttpBaseUrl() {
    return "http://" + getHost() + ":" + getHttpPort();
  }
}
