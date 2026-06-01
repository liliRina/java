package car.leasing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import car.leasing.clients.Clients;
import car.leasing.clients.ClientsDB;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestClients {
    static ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Mock
    Scanner mockScanner;
    @Mock
    ClientsDB mockClientsDB;
    @InjectMocks
    Clients clients;
    @InjectMocks
    MainHandler handler;

    @BeforeAll
    static void setUp() {
        System.setOut(new PrintStream(baos));
    }

    @Test
    @DisplayName("Добавление клиента с некорректным именем")
    public void invalidFullName() {
        when(mockScanner.nextLine())
                .thenReturn("12345")          // недопустимое имя (цифры)
                .thenReturn("Nia'am")
                .thenReturn("0");
        boolean returnAns = clients.addClient();
        String ansAddCar = baos.toString();
        assertEquals(2, countOccurrences(ansAddCar, "Имя должно содержать буквы и разделители пробел и -"));
        assertEquals(false, returnAns);
    }
    @Test
    @DisplayName("Добавление клиента с некорректным номером паспорта")
    public void invalidPassport() {
        when(mockScanner.nextLine())
                .thenReturn("Сидоров Сидор Сидорович")
                .thenReturn("12345")          // неправильный паспорт
                .thenReturn("+71234567891")
                .thenReturn("0");
        boolean returnAns = clients.addClient();
        String ansAddCar = baos.toString();
        assertEquals(2, countOccurrences(ansAddCar, "Номер паспорта должен состоять из 10 цифр"));
        assertEquals(false, returnAns);
    }
    @Test
    @DisplayName("Добавление клиента с некорректным номером телефона")
    public void invalidPhone() {
        when(mockScanner.nextLine())
                .thenReturn("Кузнецова Анна Ивановна")
                .thenReturn("1234567890")
                .thenReturn("+7123")          // неправильный телефон
                .thenReturn("71234567892")
                .thenReturn("0");
        boolean returnAns = clients.addClient();
        String ansAddCar = baos.toString();
        assertEquals(2, countOccurrences(ansAddCar, "Номер телефона должен состоять +7 и 10 цифр"));
        assertEquals(false, returnAns);
    }
    @Test
    @DisplayName("Добавление клиента с корректными параметрами")
    public void createNewClient() {
        when(mockClientsDB.getClientByPassportNumber(any())).thenReturn(null);
        when(mockClientsDB.getClientByPhoneNumber(any())).thenReturn(null);

        when(mockClientsDB.saveNewClient(any())).thenReturn(true);
        when(mockScanner.nextLine())
                .thenReturn("Петров Петр Петрович")
                .thenReturn("9876543210")
                .thenReturn("+79876543210")
                .thenReturn("0");
        boolean returnAns = clients.addClient();
        String ansAddClient = baos.toString();
        assertEquals(1, countOccurrences(ansAddClient, "Клиент успешно добавлен!"));
        assertEquals(true, returnAns);
    }

    private long countOccurrences(String text, String word) {
        return Pattern.compile(Pattern.quote(word))
                .matcher(text)
                .results()
                .count();
    }
}
