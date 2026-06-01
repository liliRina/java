package car.leasing.cars.domain;

import jakarta.persistence.*;
import lombok.*;
import car.leasing.contracts.listeners.CarDeleteListener;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "cars")
@EntityListeners(CarDeleteListener.class)
public class Car {
    @Id
    @Column(length = 17, nullable = false)
    @EqualsAndHashCode.Include
    private String VIN;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.Available;

    public Car(
            String VIN,
            String brand,
            String model,
            Integer year,
            BigDecimal price,
            Status status
    ){
        this.VIN = VIN.strip();
        this.brand = brand.strip();
        this.model = model.strip();
        this.year = year;
        this.price = price;
        this.status = status == null ? Status.Available : status;
    }

    public enum Status {
        Available,
        InUse;

        @Override
        public String toString() {
            return switch (this) {
                case Available -> "доступен";
                case InUse -> "в лизинге";
            };
        }
        public static Status fromString(String val){
            return switch (val){
                case "доступен" -> Available;
                case "в лизинге" -> InUse;
                default -> Available;
            };
        }
    }
    public static boolean checkVIN(String VIN){
        if (VIN == null)
            return false;
        VIN = VIN.strip();
        return VIN.length() == 17
                && VIN.chars().allMatch(c -> c >= 'A'&& c <= 'Z' ||
                                                    c >='a' && c <= 'z' ||
                                                    c >= '0' && c <= '9');
    }
    public static boolean checkYear(Integer year){
        if (year == null)
            return false;
        if (year >= 2000 && year <= 2026)
            return true;
        else
            return false;
    }
    @Override
    public String toString(){
        return "VIN: " + VIN + " " + brand + " " + model + ", " +
                year +" г. - " + price + " (" + status + ")";
    }
}
