package dto;

import enums.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserRequestDTO {

    @NotNull
    @NotBlank(message = "O nome não pode ser nulo")
    @Size(min = 5, message = "Nomes com pelo menos 5 caracteres")
    private String name;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email inválido")
    private String email;

    @NotNull
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula e um caractere especial.")
    private String password;

    @NotNull(message = "O campo 'Role' é obrigatório")
    private Role role;

    @NotBlank
    @Size(min = 6, message = "A frase secreta deve ter no minimo 6 caracteres")
    private String secretPhrase;

}
