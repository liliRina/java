package DB;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "Koloboks",  // Имя таблицы (если не указать = имя класса)
uniqueConstraints = {
        @UniqueConstraint(
        name = "uniqueName", // Имя ограничения в БД
        columnNames = {"name"}) // Колонки для уникальности
        }
)
@Data
@RequiredArgsConstructor
public class Kolobok implements EntityFromTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name",
            nullable = false,
            length = 50)
    @NonNull private String name;

    @Column(name = "speed", precision = 10)
    @NonNull private Integer speed;

    @Column(name = "roundness", precision = 10)
    @NonNull private Double roundness;

    @Embedded
    private Dough dough;

    @Enumerated(EnumType.STRING)
    @Column(name = "eyes_color")
    @NonNull private EyesColor eyesColor;

    //Read-only связь
    @ManyToOne(cascade = CascadeType.PERSIST)
    @NotFound(action = NotFoundAction.IGNORE) // нет муки в калориях и неважно
    @JoinColumn(name = "flour_trademark",
            referencedColumnName = "flour_trademark", // ссылаемся на unique поле
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), // не требует, чтобы значение было в Calorie
            insertable = false, // позволяет мапить
            updatable = false)  // на колонку 2 сущности
    private Calorie calorie;

    public Kolobok(){
        new Kolobok("defaultKolobok", 0, 1.0, EyesColor.Blue);
    }

    private String runnerAhead;

    private String runnerbehind;
}

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
class Dough{
    @Column(name = "water_amount")
    private Double waterAmount;

    @Column(name = "flour_trademark")
    private String flourTrademark;

    @Column(name = "is_yeast")
    private boolean isYeast;
}

enum EyesColor{
    Blue(1),
    Green(2),
    Brown(3),
    Grey(4);

    private final int number;
    private final static int divider = 4;

    EyesColor(int num){
        number = num;
    }

    public static EyesColor fromInt(int num){
        num %= divider;
        switch (num){
            case 0:
                return Blue;
            case 1:
                return Green;
            case 2:
                return Brown;
            default:
                return Grey;
        }
    }
}
