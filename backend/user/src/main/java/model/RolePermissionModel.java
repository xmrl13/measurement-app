package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("role_permissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissionModel {

    @Id
    private Long id;
    private String role;
    private String action;
    private boolean active;


    public RolePermissionModel(String role, String action, boolean active) {
        this.role = role;
        this.action = action;
        this.active = active;
    }
}
