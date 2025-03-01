package message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PermissionEventDTO {

    @JsonProperty("role")
    private String role;
    @JsonProperty("action")
    private String action;
    @JsonProperty("eventType")
    private String eventType;
    @Getter
    @JsonProperty("active")
    private boolean active;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionEventDTO that = (PermissionEventDTO) o;
        return active == that.active && Objects.equals(role, that.role) && Objects.equals(action, that.action) && Objects.equals(eventType, that.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, action, eventType, active);
    }
}
