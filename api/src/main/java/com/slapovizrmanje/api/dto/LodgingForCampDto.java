package com.slapovizrmanje.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LodgingForCampDto {
  private int tent;
  private int caravan;
  private int car;
  private int sleepingBag;
}
