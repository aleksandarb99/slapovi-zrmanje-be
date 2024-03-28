package com.slapovizrmanje.shared.dto;

import com.slapovizrmanje.shared.model.enums.Language;
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
public class CampPriceRequestDTO {
  @Valid
  @NotNull(message = "{" +
          "\"EN\": \"Field 'language' has to be provided\"," +
          "\"DE\": \"Feld 'Sprache' muss angegeben werden\"," +
          "\"HR\": \"Polje 'jezik' mora biti navedeno\"" +
          "}")
  private Language language;
  @Valid
  @NotNull(message = "{" +
          "\"EN\": \"Field 'guests' has to be provided\"," +
          "\"DE\": \"Feld 'Gäste' muss angegeben werden\"," +
          "\"HR\": \"Polje 'gosti' mora biti navedeno\"" +
          "}")
  private GuestsDTO guests;
  @Valid
  @NotNull(message = "{" +
          "\"EN\": \"Field 'lodging' has to be provided\"," +
          "\"DE\": \"Feld 'Unterkunft' muss angegeben werden\"," +
          "\"HR\": \"Polje 'smještaj' mora biti navedeno\"" +
          "}")
  private Map<String, Integer> lodging;
  private boolean powerSupply;
  @Future(message = "{" +
          "\"EN\": \"Field 'startDate' has to be in the future\"," +
          "\"DE\": \"Feld 'Startdatum' muss in der Zukunft liegen\"," +
          "\"HR\": \"Polje 'početniDatum' mora biti u budućnosti\"" +
          "}")
  @NotNull(message = "{" +
          "\"EN\": \"Field 'startDate' has to be provided\"," +
          "\"DE\": \"Feld 'Startdatum' muss angegeben werden\"," +
          "\"HR\": \"Polje 'početniDatum' mora biti navedeno\"" +
          "}")
  private LocalDate startDate;
  @Future(message = "{" +
          "\"EN\": \"Field 'endDate' has to be in the future\"," +
          "\"DE\": \"Feld 'Enddatum' muss in der Zukunft liegen\"," +
          "\"HR\": \"Polje 'završniDatum' mora biti u budućnosti\"" +
          "}")
  @NotNull(message = "{" +
          "\"EN\": \"Field 'endDate' has to be provided\"," +
          "\"DE\": \"Feld 'Enddatum' muss angegeben werden\"," +
          "\"HR\": \"Polje 'završniDatum' mora biti navedeno\"" +
          "}")
  private LocalDate endDate;
}
