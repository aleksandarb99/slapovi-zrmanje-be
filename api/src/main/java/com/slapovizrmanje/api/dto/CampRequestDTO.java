package com.slapovizrmanje.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampRequestDTO {
  @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email address provided")
  private String email;
  @NotBlank(message = "Field 'firstName' has to be provided")
  private String firstName;
  @NotBlank(message = "Field 'lastName' has to be provided")
  private String lastName;
  @Valid
  @NotNull(message = "Field 'guests' has to be provided")
  private CampGuestsDTO guests;
  @Valid
  @NotNull(message = "Field 'lodging' has to be provided")
  private CampLodgingDTO lodging;
  private boolean powerSupply;
  private LocalDate startDate;
  private LocalDate endDate;
}
