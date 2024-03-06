package com.slapovizrmanje.shared.dto;

import com.slapovizrmanje.shared.model.enums.AccommodationType;
import com.slapovizrmanje.shared.model.enums.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationRequestDTO {
  @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email address provided")
  @NotBlank(message = "Field 'email' has to be provided")
  private String email;
  @NotBlank(message = "Field 'firstName' has to be provided")
  private String firstName;
  @NotBlank(message = "Field 'lastName' has to be provided")
  private String lastName;
  @NotNull(message = "Field 'type' has to be provided")
  private AccommodationType type;
  @NotNull(message = "Field 'language' has to be provided")
  private Language language;
  @Valid
  @NotNull(message = "Field 'guests' has to be provided")
  private GuestsDTO guests;
  @Valid
  @NotNull(message = "Field 'lodging' has to be provided")
  private Map<String, Integer> lodging;
  private boolean powerSupply;
  @Future
  @NotNull(message = "Field 'startDate' has to be provided")
  private LocalDate startDate;
  @Future
  @NotNull(message = "Field 'endDate' has to be provided")
  private LocalDate endDate;
}
