package car.leasing.clients.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClientRequest (
    @NotBlank(message = "ФИО не может быть пустым")
    @Pattern(regexp = "^[A-Za-zА-Яа-я]+(?:[ -][A-Za-zА-Яа-я]+)*$", message = "ФИО должно содержать буквы и разделители: пробел и -")
    String fullName,

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "^\\d{10}$", message = "Паспорт должен состоять из 10 цифр")
    String passportNumber,

    @NotBlank(message = "Телефонный номер не может быть пустым")
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Телефон должен иметь формат \"+7XXXXXXXXXX\"")
    String phoneNumber,

    @NotBlank(message = "Логин не может быть пустым")
    String login,
    @NotBlank(message = "Пароль не может быть пустым")
    String password
){public ClientRequest(String fullName, String passportNumber, String phoneNumber,
                       String login, String password){
    this.fullName = fullName.strip();
    this.passportNumber = passportNumber.strip();
    this.phoneNumber = phoneNumber;
    this.login = login.strip();
    this.password = password.strip();
}};
