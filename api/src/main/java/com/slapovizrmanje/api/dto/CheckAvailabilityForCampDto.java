package com.slapovizrmanje.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckAvailabilityForCampDto {
//  TODO: Add field validation
  private String email;
  private String firstName;
  private String lastName;
  private GuestsForCampDto guests;
  private LodgingForCampDto lodging;
  private boolean powerSupply;
  private LocalDate startDate;
  private LocalDate endDate;
}