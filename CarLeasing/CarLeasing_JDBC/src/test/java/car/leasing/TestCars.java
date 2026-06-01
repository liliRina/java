package car.leasing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestCars {
    private static Connection h2Conn;
    private PrintStream originalOut;

    @Mock
    Scanner mockScanner;
    @InjectMocks
    MainHandler handler;

    @BeforeEach
    void saveOriginal() {
        originalOut = System.out;
    }

    @AfterEach
    void restore() {
        System.setOut(originalOut);
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

    @BeforeAll
    static void setUpH2() throws SQLException {
        h2Conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement stmt = h2Conn.createStatement()) {
            String sqlCreateCarStatus = "CREATE TYPE car_status AS ENUM ('доступен', 'в лизинге')";
            stmt.execute(sqlCreateCarStatus);
            stmt.execute("CREATE TABLE cars (" +
                    "vin VARCHAR(17) PRIMARY KEY, " +
                    "brand VARCHAR(50) NOT NULL, " +
                    "model VARCHAR(50) NOT NULL, " +
                    "\"year\" INT, " +
                    "price DECIMAL(15,6), " +
                    "status car_status Default 'доступен')");

            stmt.execute("INSERT INTO cars (vin, brand, model, \"year\", price, status) " +
                    "VALUES ('1HGCM82633A123456', 'Honda', 'Accord', 2020, 25000.000000, 'доступен')");
            stmt.execute("INSERT INTO cars (vin, brand, model, \"year\", price, status) " +
                    "VALUES ('2FMDK3GC3DBA12345', 'Ford', 'Focus', 2023, 22000.500000, 'доступен')");
            stmt.execute("INSERT INTO cars (vin, brand, model, \"year\", price, status) " +
                    "VALUES ('3T1BF1FKXEU123456', 'Toyota', 'Camry', 2025, 32000.000000, 'доступен')");
            stmt.execute("INSERT INTO cars (vin, brand, model, \"year\", price, status) " +
                    "VALUES ('4NPD84LF2KH123456', 'Hyundai', 'Elantra', 2021, 21000.990000, 'доступен')");
        }
    }

    @Test
    void testAddCar() throws SQLException {
        invalidVIN();
        invalidYear();
        invalidPrice();
        createSameCar(); //добавляем ту, что уже есть в таблице
        createCar(); // добавляем новую
    }
    @Test
    void testFindCar() throws SQLException {

    }

    private void createSameCar() {
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1HGCM82633A123456")
                .thenReturn("Honda")
                .thenReturn("Accord")
                .thenReturn("2020")
                .thenReturn("0");

        when(mockScanner.nextBigDecimal())
                .thenReturn(BigDecimal.valueOf(25000.00));

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            {
                Runnable runn = () -> {
                    try {
                        Connection conn = InitDB.getConnection();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * FROM cars");
                        System.out.println("Чекаем");
                        while (rs.next()) {
                            System.out.println("VIN: " + rs.getString("vin"));
                            System.out.println("Brand: " + rs.getString("brand"));
                            System.out.println("Year: " + rs.getInt("year"));
                            System.out.println("Price: " + rs.getBigDecimal("price"));
                            System.out.println("Status: " + rs.getString("status"));
                            System.out.println("---");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                };
                String ansAddCar2 = captureOutput(runn);
                String ansAddCar = captureOutput(handler::mainMenu);
                assertEquals(1, countOccurrences(ansAddCar, "Машина с таким VIN уже существует"));
            }
        }
    }
    private void createCar() {
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("2001")
                .thenReturn("0");

        when(mockScanner.nextBigDecimal())
                .thenReturn(BigDecimal.valueOf(30_000.89));

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String ansAddCar = captureOutput(handler::mainMenu);
            restore();
            assertEquals(1, countOccurrences(ansAddCar, "Автомобиль успешно добавлен!"));
        }
    }
    private void invalidPrice() {
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("2001")
                .thenReturn("0");

        when(mockScanner.nextBigDecimal())
                .thenThrow(new InputMismatchException())
                .thenReturn(BigDecimal.valueOf(0));

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String ansAddCar = captureOutput(handler::mainMenu);
            restore();
            assertEquals(1, countOccurrences(ansAddCar, "Стоимость должна быть положительным числом"));
        }
    }
    public void invalidVIN(){
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("VIN123")
                .thenReturn("1234567890qwertyut")
                .thenReturn("1234567890qwerty-")
                .thenReturn("0");
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String ansAddCar = captureOutput(handler::mainMenu);
            assertEquals(3, countOccurrences(ansAddCar, "VIN должен состоять из 17 знаков: цифры и латинские буквы"));
        }
    }
    public void invalidYear(){
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("1999")
                .thenReturn("2050")
                .thenReturn("234t")
                .thenReturn("0");
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String ansAddCar = captureOutput(handler::mainMenu);
            assertEquals(3, countOccurrences(ansAddCar, "Год должен быть в диапазоне от 2000 до 2026"));
        }
    }
}
