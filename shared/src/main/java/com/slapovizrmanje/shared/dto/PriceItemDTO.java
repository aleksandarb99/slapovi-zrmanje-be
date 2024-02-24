package com.slapovizrmanje.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceItemDTO {
  private String name;
  private int count;
  private int nights;
  private double price;
}
