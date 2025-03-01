package dto;

import enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsDTO {

    private String name;
    private String email;
    private Role role;

}
