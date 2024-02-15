package com.slapovizrmanje.api.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampGuestsDTO {
  @Min(value = 0, message = "Field 'adults' has to be a positive value")
  private int adults;
  @Min(value = 0, message = "Field 'children' has to be a positive value")
  private int children;
  @Min(value = 0, message = "Field 'infants' has to be a positive value")
  private int infants;
  @Min(value = 0, message = "Field 'pets' has to be a positive value")
  private int pets;
}
