package dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class UserUpdateDTO {

    @Setter
    @Size(min = 5, message = "Nomes com ao menos 5 caracteres")
    @Nullable
    private String name;

    @Setter
    @NotNull
    @NotBlank(message = "Email atual é obrigatório")
    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email inválido")
    private String oldEmail;


    @Setter
    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email inválido")
    @Nullable
    private String newEmail;

    @Setter
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula e um caractere especial.")
    @Nullable
    private String oldPassword;

    @Setter
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula e um caractere especial.")
    @Nullable
    private String newPassword;

    @Size(min = 6, message = "A frase secreta deve ter no minimo 6 caracteres")
    @Nullable
    private String secretPhrase;

}
