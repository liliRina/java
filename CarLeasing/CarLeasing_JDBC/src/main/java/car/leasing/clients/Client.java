package car.leasing.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Client {
    private Integer ID;
    private String fullName;
    private String passportNumber;
    private String phoneNumber;

    @JsonCreator
    public Client(
            @JsonProperty("id") Integer ID,
            @JsonProperty("fullName") String fullName,
            @JsonProperty("passportNumber") String passportNumber,
            @JsonProperty("phoneNumber") String phoneNumber
    ){
        if (fullName == null || passportNumber == null || phoneNumber == null)
            throw new ClientCreateException("Not allowed null");

        if (!(checkFullName(fullName)))
            throw new ClientCreateException("Invalid name: required letters and separators space and -");
        if (!checkPassportNumber(passportNumber))
            throw new ClientCreateException("Invalid passport number: required 10 digits");
        if (!checkPhoneNumber(phoneNumber))
            throw new ClientCreateException("Invalid phone number: required '+7' and 10 digits");

        this.ID = ID;
        this.fullName = fullName.strip();
        this.passportNumber = passportNumber.strip();
        this.phoneNumber = phoneNumber.strip();
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
class ClientCreateException extends RuntimeException{
    ClientCreateException(String message){
        super(message);
    }
}