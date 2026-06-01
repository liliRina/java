package car.leasing.contracts;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class Payment {
    private final Integer contractId;
    private final Integer number;
    private final BigDecimal payment;
    private Status status;

    public Payment(
            Integer contractId,
            Integer number,
            BigDecimal payment,
            Status status
    ){
        if (contractId == null || number == null || payment == null)
            throw new PaymentCreateException("Not allowed null");
        if (payment.compareTo(BigDecimal.ZERO) < 0)
            throw new PaymentCreateException("Invalid payment: required positive number");
        this.contractId = contractId;
        this.number = number;
        this.payment = payment.setScale(6, RoundingMode.HALF_UP);
        this.status = status;
    }
    public Payment(
            Integer contractId,
            Integer number,
            BigDecimal payment
    ){
        this(contractId, number, payment, Status.NOT_PAID);
    }

    @Override
    public String toString(){
        return "Платёж №" + number + " " + payment + " [" + status + "]";
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    enum Status{
        PAID,
        NOT_PAID;
        @Override
        public String toString(){
            return switch (this){
                case PAID -> "оплачен";
                case NOT_PAID -> "не оплачен";
            };
        }
        static public Status fromString(String st){
            return switch (st){
                case "оплачен" -> PAID;
                case "не оплачен" -> NOT_PAID;
                default -> NOT_PAID;
            };
        }
    }
}
@Slf4j
class PaymentCreateException extends RuntimeException{
    PaymentCreateException(String message){
        super(message);
        log.error("Ошибка создания платежа: {}", message);
    }
}
