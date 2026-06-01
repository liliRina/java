package car.leasing.integTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import car.leasing.InitDB;
import car.leasing.MainHandler;

@ExtendWith(MockitoExtension.class)
public class TestClients {
    private static Connection h2Conn;

    @Mock
    Scanner mockScanner;
    MainHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MainHandler(mockScanner);
    }

    @BeforeAll
    static void setUpH2() throws SQLException {
        h2Conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement stmt = h2Conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS clients (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "passport_number VARCHAR(10) UNIQUE NOT NULL, " +
                    "phone_number VARCHAR(12) UNIQUE NOT NULL)");
            stmt.execute("INSERT INTO clients (full_name, passport_number, phone_number) " +
                    "VALUES ('Иванов Иван Иванович', '1234567890', '+71234567890')");
        }
    }
    @Test
    public void invalidFullName() {
        when(mockScanner.nextLine())
                .thenReturn("2")
                .thenReturn("1")
                .thenReturn("12345")          // недопустимое имя (цифры)
                .thenReturn("Nia'")
                .thenReturn("0");
        tryDo(2, "Имя должно содержать буквы и разделители пробел и -");
    }
    @Test
    public void invalidPhone() {
        when(mockScanner.nextLine())
                .thenReturn("2")
                .thenReturn("1")
                .thenReturn("Кузнецова Анна Ивановна")
                .thenReturn("1234567890")
                .thenReturn("+7123")          // неправильный телефон
                .thenReturn("71234567892")
                .thenReturn("0");

        tryDo(2, "Номер телефона должен состоять +7 и 10 цифр");
    }
    @Test
    public void invalidPassport() {
        when(mockScanner.nextLine())
                .thenReturn("2")
                .thenReturn("1")
                .thenReturn("Сидоров Сидор Сидорович")
                .thenReturn("12345")          // неправильный паспорт
                .thenReturn("+71234567891")
                .thenReturn("0");
        tryDo(2, "Номер паспорта должен состоять из 10 цифр");
    }
    @Test
    public void createSameClient() {
        when(mockScanner.nextLine())
                .thenReturn("2")         // меню клиентов
                .thenReturn("1")         // добавить клиента
                .thenReturn("Иванов Иван Иванович")
                .thenReturn("1234567890")
                .thenReturn("+71234567890")
                .thenReturn("0");
        tryDo(1, "Клиент с таким паспортом существует");
    }
    @Test
    public void createNewClient() {
        when(mockScanner.nextLine())
                .thenReturn("2")
                .thenReturn("1")
                .thenReturn("Петров Петр Петрович")
                .thenReturn("9876543210")
                .thenReturn("+79876543210")
                .thenReturn("0");
        tryDo(1, "Клиент успешно добавлен!");
    }

    private void tryDo(int cnt, String out){
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String ansAddClient = captureOutput(handler::mainMenu);
            assertEquals(cnt, countOccurrences(ansAddClient, out));
        }
    }

    private String captureOutput(Runnable action) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        action.run();
        return baos.toString();
    }

    private long countOccurrences(String text, String word) {
        return Pattern.compile(Pattern.quote(word))
                .matcher(text)
                .results()
                .count();
    }
}