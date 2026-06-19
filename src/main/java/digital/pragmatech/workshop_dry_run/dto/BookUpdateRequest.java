package digital.pragmatech.workshop_dry_run.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import digital.pragmatech.workshop_dry_run.entity.BookStatus;

/**
 * DTO for updating a book. Only library-internal fields and status are mutable;
 * bibliographic data (title, author, thumbnail) is fixed once enriched from OpenLibrary.
 */
public record BookUpdateRequest(
  @NotBlank(message = "Internal name is required")
  String internalName,

  @NotNull(message = "Availability date is required")
  LocalDate availabilityDate,

  @NotNull(message = "Status is required")
  BookStatus status
) { }
