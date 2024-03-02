package com.slapovizrmanje.shared.dto;

import com.slapovizrmanje.shared.model.Accommodation;
import com.slapovizrmanje.shared.model.enums.ScheduledEmailType;
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
  private ScheduledEmailType type;
  private List<Accommodation> accommodations;
}
