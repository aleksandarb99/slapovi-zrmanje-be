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
    @Size(max = 50, message = "Field 'name' cannot be longer than 50 characters.")
    @NotBlank(message = "Field 'name' has to be provided.")
    private String name;
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email address provided")
    @NotBlank(message = "Field 'email' has to be provided")
    private String email;
    @Size(max = 1000, message = "Field 'message' cannot be longer than 1000 characters.")
    @NotBlank(message = "Field 'message' has to be provided.")
    private String message;
}
