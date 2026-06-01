package car.leasing.clients.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ClientResponse {
    @NotNull
    private final Long id;

    @NotBlank(message = "ФИО не может быть пустым")
    @Pattern(regexp = "^[A-Za-zА-Яа-я]+(?:[ -][A-Za-zА-Яа-я]+)*$", message = "ФИО должно содержать буквы и разделители: пробел и -")
    private final String fullName;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "^\\d{10}$", message = "Паспорт должен состоять из 10 цифр")
    private final String passportNumber;

    @NotBlank(message = "Телефонный номер не может быть пустым")
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен иметь формат \"+7XXXXXXXXXX\"")
    private final String phoneNumber;

    @NotBlank(message = "Логин не может быть пустым")
    private final String login;

    public ClientResponse(Long id, String fullName, String passportNumber, String phoneNumber, String login){
        this.id = id;
        this.fullName = fullName.strip();
        this.passportNumber = passportNumber.strip();
        this.phoneNumber = phoneNumber.strip();
        this.login = login.strip();
    }
}

