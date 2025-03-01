package dto;

import enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {

    private String name;
    private String password;
    private String email;
    private Role role;

    public UserResponseDTO() {
    }
}
