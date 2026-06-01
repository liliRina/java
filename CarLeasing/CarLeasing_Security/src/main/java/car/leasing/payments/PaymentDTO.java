package car.leasing.payments;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
public class PaymentDTO {
    @NotNull
    @Positive(message = "Номер договора должен быть положительным целым числом")
    private final Long contractId;
    @NotNull
    @Positive(message = "Номер платежа быть положительным целым числом")
    private final Integer number;
    @NotNull
    @Positive(message = "Платёж быть положительным числом")
    private final BigDecimal payment;

    public PaymentDTO(
            Long contractId,
            Integer number,
            BigDecimal payment,
            Payment.Status status
    ) {
        this.contractId = contractId;
        this.number = number;
        this.payment = payment.setScale(6, RoundingMode.HALF_UP);
    }
}
