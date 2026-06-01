package car.leasing.contracts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class Payment {
    private Integer number;
    private BigDecimal payment;
    private Status status;

    @JsonCreator
    public Payment(
            @JsonProperty("number") Integer number,
            @JsonProperty("payment") BigDecimal payment
    ){
        if (number == null || payment == null)
            throw new PaymentCreateException("Not allowed null");
        if (payment.compareTo(BigDecimal.ZERO) < 0)
            throw new PaymentCreateException("Invalid payment: required positive number");
        this.number = number;
        this.payment = payment.setScale(6, RoundingMode.HALF_UP);
        this.status = Status.NOT_PAID;
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
    }
}
class PaymentCreateException extends RuntimeException{
    PaymentCreateException(String message){
        super(message);
    }
}
