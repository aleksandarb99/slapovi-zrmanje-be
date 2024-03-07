package com.slapovizrmanje.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactQuestionDTO {
  @Size(max = 50, message = "{" +
          "\"EN\": \"Field 'name' cannot be longer than 50 characters.\"," +
          "\"DE\": \"Das Feld 'Name' darf nicht länger als 50 Zeichen sein.\"," +
          "\"HR\": \"Polje 'naziv' ne smije biti duže od 50 znakova.\"" +
          "}")
  @NotBlank(message = "{" +
          "\"EN\": \"Field 'name' has to be provided.\"," +
          "\"DE\": \"Feld 'Name' muss angegeben werden.\"," +
          "\"HR\": \"Polje 'naziv' mora biti navedeno.\"" +
          "}")
  private String name;
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
  @Size(max = 1000, message = "{" +
          "\"EN\": \"Field 'message' cannot be longer than 1000 characters.\"," +
          "\"DE\": \"Das Feld 'Nachricht' darf nicht länger als 1000 Zeichen sein.\"," +
          "\"HR\": \"Polje 'poruka' ne smije biti duže od 1000 znakova.\"" +
          "}")
  @NotBlank(message = "{" +
          "\"EN\": \"Field 'message' has to be provided.\"," +
          "\"DE\": \"Feld 'Nachricht' muss angegeben werden.\"," +
          "\"HR\": \"Polje 'poruka' mora biti navedeno.\"" +
          "}")
  private String message;
}
