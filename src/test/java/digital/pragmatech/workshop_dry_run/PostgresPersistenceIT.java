package digital.pragmatech.workshop_dry_run;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import digital.pragmatech.workshop_dry_run.entity.Book;
import digital.pragmatech.workshop_dry_run.entity.BookStatus;
import digital.pragmatech.workshop_dry_run.support.AbstractIntegrationTest;
import digital.pragmatech.workshop_dry_run.support.BookMother;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the <b>PostgreSQL (Docker)</b> part of the local setup: the Flyway migrations apply
 * against the Postgres Testcontainer (the context only starts if {@code ddl-auto: validate} matches
 * the migrated schema), and an entity round-trips through Spring Data JPA.
 */
@DisplayName("PostgreSQL setup check — Flyway migrates and entities persist")
class PostgresPersistenceIT extends AbstractIntegrationTest {

  @Test
  void shouldPersistAndReadBackBook() {
    Book saved = bookRepository.save(BookMother.availableBook());

    assertThat(saved.getId()).isNotNull();
    assertThat(bookRepository.findByIsbn("978-0-13-468599-1"))
      .hasValueSatisfying(book -> {
        assertThat(book.getTitle()).isEqualTo("Clean Code");
        assertThat(book.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(book.getStatus()).isEqualTo(BookStatus.AVAILABLE);
      });
    assertThat(bookRepository.count()).isEqualTo(1);
  }
}
