package digital.pragmatech.workshop_dry_run.entity;

/**
 * Enum representing the possible statuses of a book in the library system.
 */
public enum BookStatus {
  AVAILABLE, // Book is available for borrowing
  BORROWED,  // Book is currently borrowed by a user
  RESERVED,  // Book is reserved by a user
  MAINTENANCE // Book is under maintenance or repair
}
