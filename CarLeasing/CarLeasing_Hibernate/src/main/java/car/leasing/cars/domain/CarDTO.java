package car.leasing.cars.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
public class CarDTO {

    @NotBlank(message = "VIN не может быть пустым")
    @Pattern(regexp = "^[A-Za-z0-9]{17}$", message = "VIN должен содержать 17 символов: латинские буквы и цифры")
    private String VIN;

    @NotBlank(message = "Пустое значение марки недопустимо")
    private String brand;

    @NotBlank(message = "Пустое значение модели недопустимо")
    private String model;

    @NotNull(message = "Год обязателен")
    @Min(value = 2000, message = "Год должен быть в диапазоне от 2000 до 2026")
    @Max(value = 2026, message = "Год должен быть в диапазоне от 2000 до 2026")
    private Integer year;

    @NotNull(message = "Цена обязателена")
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;

    @JsonCreator
    public CarDTO(
            @JsonProperty("vin") String VIN,
            @JsonProperty("brand") String brand,
            @JsonProperty("model") String model,
            @JsonProperty("year") Integer year,
            @JsonProperty("price") BigDecimal price
    ){
        this.VIN = VIN.strip();
        this.brand = brand.strip();
        this.model = model.strip();
        this.year = year;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }
}
