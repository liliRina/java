package car.leasing.contracts;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ContractRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9]{17}$", message = "VIN должен содержать 17 символов: латинские буквы и цифры")
    private String carVin;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "^\\d{10}$", message = "Паспорт должен состоять из 10 цифр")
    private String clientPassport;

    @NotNull
    @Positive(message = "Количество месяцев должно быть положительным целым числом")
    private Integer monthsCnt;

    @NotNull
    @Positive(message = "Начальный платёж должен быть положительным числом")
    private BigDecimal initialPayment;

    @NotNull
    @Min(value = 0, message = "Процентная ставка должна быть положительным числом от 0 до 100")
    @Max(value = 100, message = "Процентная ставка должна быть положительным числом от 0 до 100")
    private Double rate;
}