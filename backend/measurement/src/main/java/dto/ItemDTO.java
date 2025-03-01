package dto;

import lombok.Getter;
import lombok.Setter;


public class ItemDTO {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String unit;

    public ItemDTO(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    public ItemDTO() {
    }
}
