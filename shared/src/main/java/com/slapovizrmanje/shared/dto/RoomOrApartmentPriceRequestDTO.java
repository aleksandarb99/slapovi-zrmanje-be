package com.slapovizrmanje.shared.dto;

import com.slapovizrmanje.shared.model.enums.AccommodationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomOrApartmentPriceRequestDTO {
  @NotNull(message = "Field 'type' has to be provided")
  private AccommodationType type;
  @Valid
  @NotNull(message = "Field 'guests' has to be provided")
  private GuestsDTO guests;
  @Valid
  @NotNull(message = "Field 'lodging' has to be provided")
  private Map<String, Integer> lodging;
  @Future
  @NotNull(message = "Field 'startDate' has to be provided")
  private LocalDate startDate;
  @Future
  @NotNull(message = "Field 'endDate' has to be provided")
  private LocalDate endDate;
}
