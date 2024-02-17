package com.slapovizrmanje.shared.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampLodgingDTO {
  @Min(value = 0, message = "Field 'tent' has to be a positive value")
  private int tent;
  @Min(value = 0, message = "Field 'caravan' has to be a positive value")
  private int caravan;
  @Min(value = 0, message = "Field 'car' has to be a positive value")
  private int car;
  @Min(value = 0, message = "Field 'sleepingBag' has to be a positive value")
  private int sleepingBag;
}
