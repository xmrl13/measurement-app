package message.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("role_permissions")
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
