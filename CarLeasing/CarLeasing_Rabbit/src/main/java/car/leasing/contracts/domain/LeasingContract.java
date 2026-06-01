package car.leasing.contracts.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import car.leasing.cars.domain.Car;
import car.leasing.clients.domain.Client;
import car.leasing.exception.InvalidParameterException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Entity
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "contracts")
public class LeasingContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long contractId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "car_vin", referencedColumnName = "vin")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Car car;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Client client;

    @Column(nullable = false)
    private Integer monthsCnt;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal initialPayment;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @JsonCreator
    public LeasingContract(
            @JsonProperty("contractId") Long contractId,
            @JsonProperty("car") Car car,
            @JsonProperty("client") Client client,
            @JsonProperty("monthsCnt") Integer monthsCnt,
            @JsonProperty("initialPayment") BigDecimal initialPayment,
            @JsonProperty("rate") Double rate
    ) {
        if (initialPayment != null && initialPayment.compareTo(car.getPrice()) >= 0) {
            throw new InvalidParameterException("Начальный платёж должен быть меньше стоимости автомобиля");
        }

        BigDecimal loan = car.getPrice().subtract(initialPayment);
        BigDecimal monthRate = BigDecimal.valueOf(rate)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal paymentAmount = loan.multiply(monthRate)
                .multiply(monthRate.add(BigDecimal.ONE).pow(monthsCnt))
                .divide(monthRate.add(BigDecimal.ONE).pow(monthsCnt)
                        .subtract(BigDecimal.ONE), 6, RoundingMode.HALF_UP);


        this.contractId = contractId;
        this.car = car;
        this.client = client;
        this.monthsCnt = monthsCnt;
        this.initialPayment = initialPayment.setScale(6, RoundingMode.HALF_UP);
        this.rate = rate;
        this.monthlyPayment = paymentAmount;
        this.status = Status.ACTIVE;
    }

    public static boolean checkCntMonth(Integer cntMonth){
        return cntMonth != null && cntMonth.compareTo(0) > 0;
    }
    public static boolean checkInitialPayment(BigDecimal initialPayment, BigDecimal carPrice){
        return initialPayment != null && carPrice != null &&
                initialPayment.compareTo(carPrice) < 0 &&
                initialPayment.compareTo(BigDecimal.ZERO) > 0;
    }
    public static boolean checkRate(Double rate){
        return rate != null && rate.compareTo(0d) > 0 && rate.compareTo(100d) < 0;
    }

    @Override
    public String toString(){
        return "ДОГОВОР №" + contractId + "\n" +
                "Автомобиль: " + car.toString().split("г.")[0] + "\n" +
                "Клиент: " + client + "\n" +
                "Срок: " + monthsCnt + " месяцев" + "\n" +
                "Стоимость: " + car.getPrice() + "\n" +
                "Первоначальный взнос: " + initialPayment + "\n" +
                "Процентная ставка: " + rate;
    }

    public enum Status{
        ACTIVE,
        CLOSED;
    }
}
