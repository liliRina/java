package car.leasing.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class Client {
    private final Long ID;

    @NotBlank(message = "ФИО не может быть пустым")
    @Pattern(regexp = "^[A-Za-zА-Яа-я]+(?:[ -][A-Za-zА-Яа-я]+)*$", message = "ФИО должно содержать буквы и разделители: пробел и -")
    private final String fullName;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "^\\d{10}$", message = "Паспорт должен состоять из 10 цифр")
    private final String passportNumber;

    @NotBlank(message = "Телефонный номер не может быть пустым")
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен иметь формат \"+7XXXXXXXXXX\"")
    private final String phoneNumber;

    private final Status status;

    public Client(
            Long ID,
            String fullName,
            String passportNumber,
            String phoneNumber,
            Status status
    ){
        this.ID = ID;
        this.fullName = fullName.strip();
        this.passportNumber = passportNumber.strip();
        this.phoneNumber = phoneNumber.strip();
        this.status = status;
    }

    @JsonCreator
    public Client(
            @JsonProperty("id") Long ID,
            @JsonProperty("fullName") String fullName,
            @JsonProperty("passportNumber") String passportNumber,
            @JsonProperty("phoneNumber") String phoneNumber
    ){
        this(ID, fullName, passportNumber, phoneNumber, Status.NotActiveContract);
    }

    public enum Status{
        HasActiveContract,
        NotActiveContract;
        @Override
        public String toString() {
            return switch (this) {
                case HasActiveContract -> "есть активный договор";
                case NotActiveContract -> "нет активных договоров";
            };
        }
        @JsonCreator
        public static Client.Status fromString(String val){
            return switch (val){
                case "есть активный договор" -> HasActiveContract;
                default -> NotActiveContract;
            };
        }
    }

    @Override
    public String toString(){
        return ID + " " + fullName + " " + passportNumber + " " + phoneNumber;
    }
    public static boolean checkFullName(String name){
        if (name == null)
            return false;
        name = name.strip();
        return name.matches("^[A-Za-zА-Яа-я]+(?:[ -][A-Za-zА-Яа-я]+)*$");
    }

    public static boolean checkPassportNumber(String passportNumber){
        if (passportNumber == null)
            return false;
        passportNumber = passportNumber.strip();
        return passportNumber.length() == 10 &&
                passportNumber.chars().allMatch(c -> c >= '0' && c <= '9');
    }
    public static boolean checkPhoneNumber(String phoneNumber){
        if (phoneNumber == null)
            return false;
        phoneNumber = phoneNumber.strip();
        return phoneNumber.matches("^\\+7\\d{10}$");
    }
}