package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDTO {

    @NotBlank(message = "Digite o email")
    private String email;


}
