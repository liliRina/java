package car.leasing.contracts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import car.leasing.cars.Car;
import car.leasing.clients.Client;
import car.leasing.exception.InvalidParameterException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class LeasingContract {
    @NotNull
    @Positive(message = "Номер договора должен быть положительным целым числом")
    private Long contractNumber;
    @NotNull
    private Car car;
    @NotNull
    private Client client;
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

    private Status status;

    @JsonCreator
    public LeasingContract(
            @JsonProperty("contractNumber") Long contractNumber,
            @JsonProperty("car") Car car,
            @JsonProperty("client") Client client,
            @JsonProperty("monthsCnt") Integer monthsCnt,
            @JsonProperty("initialPayment") BigDecimal initialPayment,
            @JsonProperty("rate") Double rate
    ) {
        if (initialPayment != null && initialPayment.compareTo(car.getPrice()) >= 0) {
            throw new InvalidParameterException("Начальный платёж должен быть меньше стоимости автомобиля");
        }
        this.contractNumber = contractNumber;
        this.car = car;
        this.client = client;
        this.monthsCnt = monthsCnt;
        this.initialPayment = initialPayment.setScale(6, RoundingMode.HALF_UP);
        this.rate = rate;
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
        return "ДОГОВОР №" + contractNumber + "\n" +
                "Автомобиль: " + car.toString().split("г.")[0] + "\n" +
                "Клиент: " + client + "\n" +
                "Срок: " + monthsCnt + " месяцев" + "\n" +
                "Стоимость: " + car.getPrice() + "\n" +
                "Первоначальный взнос: " + initialPayment + "\n" +
                "Процентная ставка: " + rate;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status{
        ACTIVE,
        CLOSED;
    }
}