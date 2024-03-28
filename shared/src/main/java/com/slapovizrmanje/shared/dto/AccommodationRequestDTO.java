package com.slapovizrmanje.shared.dto;

import com.slapovizrmanje.shared.model.enums.AccommodationType;
import com.slapovizrmanje.shared.model.enums.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
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
public class AccommodationRequestDTO {
  @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "{" +
          "  \"EN\": \"Invalid email address provided\"," +
          "  \"DE\": \"Ungültige E-Mail-Adresse angegeben\"," +
          "  \"HR\": \"Navedena je nevažeća email adresa\"" +
          "}")
  @NotBlank(message = "{" +
          "\"EN\": \"Field 'email' has to be provided\"," +
          "\"DE\": \"Feld 'E-Mail' muss angegeben werden\"," +
          "\"HR\": \"Polje 'email' mora biti navedeno\"" +
          "}")
  private String email;
  @NotBlank(message = "{" +
          "\"EN\": \"Field 'firstName' has to be provided\"," +
          "\"DE\": \"Feld 'Vorname' muss angegeben werden\"," +
          "\"HR\": \"Polje 'ime' mora biti navedeno\"" +
          "}")
  private String firstName;
  @NotBlank(message = "{" +
          "\"EN\": \"Field 'lastName' has to be provided\"," +
          "\"DE\": \"Feld 'Familienname' muss angegeben werden\"," +
          "\"HR\": \"Polje 'prezime' mora biti navedeno\"" +
          "}")
  private String lastName;
  @NotNull(message = "{" +
        "\"EN\": \"Field 'type' has to be provided\"," +
        "\"DE\": \"Feld 'Typ' muss angegeben werden\"," +
        "\"HR\": \"Polje 'vrsta' mora biti navedeno\"" +
        "}")
  private AccommodationType type;
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
