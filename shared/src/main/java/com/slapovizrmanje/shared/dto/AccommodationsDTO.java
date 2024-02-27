package com.slapovizrmanje.shared.dto;

import com.slapovizrmanje.shared.model.Accommodation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationsDTO {
  List<Accommodation> accommodations;
}
