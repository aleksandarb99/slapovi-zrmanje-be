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
public class GuestsDTO {
  @Min(value = 0, message = "{" +
          "\"EN\": \"Field 'adults' has to be a positive value\"," +
          "\"DE\": \"Feld 'Erwachsene' muss einen positiven Wert haben\"," +
          "\"HR\": \"Polje 'odrasli' mora imati pozitivnu vrijednost\"" +
          "}")
  private int adults;
  @Min(value = 0, message = "{" +
          "\"EN\": \"Field 'children' has to be a positive value\"," +
          "\"DE\": \"Feld 'Kinder' muss einen positiven Wert haben\"," +
          "\"HR\": \"Polje 'djeca' mora imati pozitivnu vrijednost\"" +
          "}")
  private int children;
  @Min(value = 0, message = "{" +
          "\"EN\": \"Field 'infants' has to be a positive value\"," +
          "\"DE\": \"Feld 'Säuglinge' muss einen positiven Wert haben\"," +
          "\"HR\": \"Polje 'dojenčad' mora imati pozitivnu vrijednost\"" +
          "}")
  private int infants;
  @Min(value = 0, message = "{" +
          "\"EN\": \"Field 'pets' has to be a positive value\"," +
          "\"DE\": \"Feld 'Haustiere' muss einen positiven Wert haben\"," +
          "\"HR\": \"Polje 'ljubimci' mora imati pozitivnu vrijednost\"" +
          "}")
  private int pets;
}
