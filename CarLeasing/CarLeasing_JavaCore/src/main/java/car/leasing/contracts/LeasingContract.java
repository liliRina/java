package car.leasing.contracts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import car.leasing.cars.Car;
import car.leasing.clients.Client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
public class LeasingContract {
    private Long contractNumber;
    private Car car;
    private Client client;
    private Integer monthsCnt;
    private BigDecimal initialPayment;
    private Double rate;
    private Status status;
    private ArrayList<Payment> paymentSchedule;
    private int indCurPayment;
    @JsonCreator
    public LeasingContract(
            @JsonProperty("contractNumber") Long contractNumber,
            @JsonProperty("car") Car car,
            @JsonProperty("client") Client client,
            @JsonProperty("monthsCnt") Integer monthsCnt,
            @JsonProperty("initialPayment") BigDecimal initialPayment,
            @JsonProperty("rate") Double rate
    ) {

        if (contractNumber == null || car == null || client == null ||
        monthsCnt == null || initialPayment == null || rate == null)
            throw new LeasingContractCreateException("Not allowed null");
        if (!checkCntMonth(monthsCnt))
            throw new LeasingContractCreateException("Invalid count of months: required more than 0");

        if(!checkInitialPayment(initialPayment, car.getPrice()))
            throw new LeasingContractCreateException("Initial payment must be less than the price of the car");
        if(!checkRate(rate))
            throw new LeasingContractCreateException("Invalid rate: required more than 0 and less than 100");

        BigDecimal loan = car.getPrice().subtract(initialPayment);
        BigDecimal monthRate = BigDecimal.valueOf(rate)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal payment = loan.multiply(monthRate)
                .multiply(monthRate.add(BigDecimal.ONE).pow(monthsCnt))
                .divide(monthRate.add(BigDecimal.ONE).pow(monthsCnt)
                        .subtract(BigDecimal.ONE), 6, RoundingMode.HALF_UP);
        this.contractNumber = contractNumber;
        this.car = car;
        this.client = client;
        this.monthsCnt = monthsCnt;
        this.initialPayment = initialPayment.setScale(6, RoundingMode.HALF_UP);
        this.rate = rate;
        this.status = Status.ACTIVE;
        this.paymentSchedule = new ArrayList<>(monthsCnt);
        for(int i = 1; i <= monthsCnt; i++){
           Payment newPayment = new Payment(i, payment);
           paymentSchedule.add(newPayment);
        }
        indCurPayment = 0;
    }
    @JsonIgnore
    public Payment getCurrentPayment() {
        if (status == Status.CLOSED)
            return null;
        return paymentSchedule.get(indCurPayment);
    }
    public void showCurPayment(){
        if (status == Status.CLOSED)
            return;
        System.out.println("Текущий платёж: " +
                paymentSchedule.get(indCurPayment) +
                " (месяц " + (indCurPayment+1) + ")");
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
    @JsonIgnore
    public boolean isClosed(){
        return status == Status.CLOSED;
    }

    @Override
    public String toString(){
        return "ДОГОВОР №" + contractNumber + "\n" +
                "Автомобиль: " + car.toString().split("г.")[0] + "\n" +
                "Клиент: " + client + "\n" +
                "Срок: " + monthsCnt + " месяцев" + "\n" +
                "Стоимость: " + car.getPrice() + "\n" +
                "Первоначальный взнос: " + initialPayment + "\n" +
                "Процентная ставка: " + rate + "\n" +
                "Платежи:" + "\n" +
                paymentSchedule.stream()
                        .map(p -> p.toString())
                        .collect(Collectors.joining("\n"));
    }

    public void pay() {
        if (status == Status.CLOSED)
            return;
        paymentSchedule.get(indCurPayment).setStatus(Payment.Status.PAID);
        indCurPayment++;
        if (indCurPayment == paymentSchedule.size())
            status = Status.CLOSED;
    }

    enum Status{
        ACTIVE,
        CLOSED;
    }
}

class LeasingContractCreateException extends RuntimeException{
    public LeasingContractCreateException(String message){
        super(message);
    }
}