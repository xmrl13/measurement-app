package dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class ItemRequestDTO {

    private String name;

    private String unit;

    public ItemRequestDTO(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }
}
