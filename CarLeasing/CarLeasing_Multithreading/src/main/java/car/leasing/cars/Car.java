package car.leasing.cars;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class Car {
    private String VIN;
    private String brand;
    private String model;
    private String year;
    private BigDecimal price;
    private Status status = Status.Available;

    @JsonCreator
    public Car(
            @JsonProperty("vin") String VIN,
            @JsonProperty("brand") String brand,
            @JsonProperty("model") String model,
            @JsonProperty("year") String year,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("status") Status status
    ){
        if (VIN == null || brand == null || brand.isBlank() ||
                model == null || model.isBlank() || year == null || price == null)
            throw new CarCreateException("Not allowed null");

        if (!checkVIN(VIN))
            throw new CarCreateException("Invalid VIN: required Latin letters and numbers of length 17");

        if (!checkYear(year))
            throw new CarCreateException("Invalid year: required int from 2000 to 2026");

        this.VIN = VIN.strip();
        this.brand = brand.strip();
        this.model = model.strip();
        this.year = year.strip();
        this.price = price.setScale(6, RoundingMode.HALF_UP);
        this.status = status == null ? Status.Available : status;
    }

    public void setStatus(Status status){
        this.status = status;
    }
    public enum Status {
        Available,
        InUse;

        @Override
        public String toString() {
            return switch (this) {
                case Available -> "доступен";
                case InUse -> "в лизинге";
            };
        }
    }
    public static boolean checkVIN(String VIN){
        if (VIN == null)
            return false;
        VIN = VIN.strip();
        return VIN.length() == 17
                && VIN.chars().allMatch(c -> c >= 'A'&& c <= 'Z' ||
                                                    c >='a' && c <= 'z' ||
                                                    c >= '0' && c <= '9');
    }
    public static boolean checkYear(String year){
        if (year == null)
            return false;
        year = year.strip();
        if (year.length() != 4)
            return false;
        try{
            Integer intYear = Integer.valueOf(year);
            if (intYear >= 2000 && intYear <= 2026)
                return true;
        }
        catch (RuntimeException e){
            return false;
        }
        return false;
    }
    @Override
    public String toString(){
        return "VIN: " + VIN + " " + brand + " " + model + ", " +
                year +" г. - " + price + " (" + status + ")";
    }
}
class CarCreateException extends RuntimeException{
    CarCreateException(String message){
        super(message);
    }
}
