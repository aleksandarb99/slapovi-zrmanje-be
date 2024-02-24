package com.slapovizrmanje.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponseDTO {
  private double totalPrice;
  private List<PriceItemDTO> priceItems;
}
