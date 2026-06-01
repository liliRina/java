package car.leasing.payments;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class Payment {
    @NotNull
    @Positive(message = "Номер договора должен быть положительным целым числом")
    private final Long contractId;
    @NotNull
    @Positive(message = "Номер платежа быть положительным целым числом")
    private final Integer number;
    @NotNull
    @Positive(message = "Платёж быть положительным числом")
    private final BigDecimal payment;

    private Status status;

    public Payment(
            Long contractId,
            Integer number,
            BigDecimal payment,
            Status status
    ){
        this.contractId = contractId;
        this.number = number;
        this.payment = payment.setScale(6, RoundingMode.HALF_UP);
        this.status = status;
    }
    public Payment(
            Long contractId,
            Integer number,
            BigDecimal payment
    ){
        this(contractId, number, payment, Status.UNPAID);
    }

    @Override
    public String toString(){
        return "Платёж №" + number + " " + payment + " [" + status + "]";
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status{
        PAID,
        UNPAID;
        @Override
        public String toString(){
            return switch (this){
                case PAID -> "оплачен";
                case UNPAID -> "не оплачен";
            };
        }
        static public Status fromString(String st){
            return switch (st){
                case "оплачен" -> PAID;
                default -> UNPAID;
            };
        }
    }
}
