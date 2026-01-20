package DB;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Calorie")
@Data
@NoArgsConstructor
public class Calorie implements EntityFromTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer calorificValue;

    @Column(name = "flour_trademark", unique = true, columnDefinition = "VARCHAR(255)")
    private String flourTrademark;

    Calorie(Integer calorificValue, String flourTrademark) {
        this.calorificValue = calorificValue;
        this.flourTrademark = flourTrademark;
    }
}
