package car.leasing.payments;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "payments")
public class Payment {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private PaymentId id;

    @NotNull
    @Positive(message = "Платёж быть положительным числом")
    private BigDecimal payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public Payment(
            Long contractId,
            Integer number,
            BigDecimal payment,
            Status status
    ){
        id = new PaymentId(contractId, number);
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
        return "Платёж №" + id.getPaymentNumber() + " " + payment + " [" + status + "]";
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
@Getter
@Setter
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
class PaymentId implements Serializable {

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "payment_number")
    private Integer paymentNumber;

    public PaymentId(Long contractId, Integer paymentNumber) {
        this.contractId = contractId;
        this.paymentNumber = paymentNumber;
    }
}
