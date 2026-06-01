package car.leasing.cars;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
// на риквест на риспондс dto
// отдельно папка модели
// отдельно репрезетории
// мб лучше так?
@Getter
public class Car {
    @NotBlank(message = "VIN не может быть пустым")
    @Pattern(regexp = "^[A-Za-z0-9]{17}$", message = "VIN должен содержать 17 символов: латинские буквы и цифры")
    private String VIN;

    @NotBlank(message = "Пустое значение марки недопустимо")
    private String brand;

    @NotBlank(message = "Пустое значение модели недопустимо")
    private String model;

    @Min(value = 2000, message = "Год должен быть в диапазоне от 2000 до 2026")
    @Max(value = 2026, message = "Год должен быть в диапазоне от 2000 до 2026")
    private Integer year;

    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;

    private Status status = Status.Available;

    @JsonCreator
    public Car(
            @JsonProperty("vin") String VIN,
            @JsonProperty("brand") String brand,
            @JsonProperty("model") String model,
            @JsonProperty("year") Integer year,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("status") Status status
    ){
        this.VIN = VIN.strip();
        this.brand = brand.strip();
        this.model = model.strip();
        this.year = year;
        this.price = price.setScale(6, RoundingMode.HALF_UP);
        this.status = status == null ? Status.Available : status;
    }

    public void setStatus(Status status){
        this.status = status;
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
        @JsonCreator
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
