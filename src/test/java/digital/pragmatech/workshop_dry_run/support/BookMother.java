package digital.pragmatech.workshop_dry_run.support;

import java.time.LocalDate;

import digital.pragmatech.workshop_dry_run.entity.Book;
import digital.pragmatech.workshop_dry_run.entity.BookStatus;

/**
 * Object Mother for the {@link Book} entity.
 *
 * Centralises test data creation so that every test works with consistent,
 * self-describing fixtures. When the Book constructor changes, fix it here
 * once — all tests stay green.
 */
public final class BookMother {

  private BookMother() {
  }

  public static Book availableBook() {
    return new Book(
      "978-0-13-468599-1",
      "clean-code",
      LocalDate.of(2008, 8, 1),
      "Clean Code",
      "Robert C. Martin"
    );
  }

  public static Book borrowedBook() {
    Book book = availableBook();
    book.setStatus(BookStatus.BORROWED);
    return book;
  }

  public static Book reservedBook() {
    Book book = availableBook();
    book.setStatus(BookStatus.RESERVED);
    return book;
  }

  public static Book maintenanceBook() {
    Book book = availableBook();
    book.setStatus(BookStatus.MAINTENANCE);
    return book;
  }
}
