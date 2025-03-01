package model;

import enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Getter
@Table("users")
public class User {

    @Id
    private Long id;

    @Setter
    @Column("name")
    @NotBlank
    private String name;

    @Setter
    @Column("email")
    @NotBlank
    @Email
    private String email;

    @Setter
    @Column("password")
    @NotBlank
    private String password;

    @Setter
    @Column("role")
    private Role role;

    @Setter
    @Getter
    @NotBlank
    @Size(min = 6, message = "A frase secreta deve ter no minimo 6 caracteres")
    @Column("secret_phrase")
    private String secretPhrase;

    public User() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
}
