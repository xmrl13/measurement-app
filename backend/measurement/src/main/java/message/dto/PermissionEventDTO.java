package message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

}
