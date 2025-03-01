package model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;


@Getter
@Table("itens")
public class Item {

    @Id
    private Long id;

    @Setter
    @NotBlank
    @Column("name")
    private String name;

    @Setter
    @NotBlank
    @Column("unit")
    private String unit;

    public Item() {
    }

    public Item(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(name, item.name) && Objects.equals(unit, item.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, unit);
    }

}
