package com.slapovizrmanje.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestsForCampDto {
  private int adults;
  private int children;
  private int infants;
  private int pets;
}
