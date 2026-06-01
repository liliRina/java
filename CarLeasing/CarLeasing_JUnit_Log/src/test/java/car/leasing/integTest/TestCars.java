package car.leasing.integTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import car.leasing.InitDB;
import car.leasing.MainHandler;

// назвать конктретный!!!!
// нестабильная. Ещё одна бд. Проще через мокито
// не юнит тест!!!!!!! Надо параметризовать !!!!!!!!!!!!!!!!
// Или тестовые данные в отдельном классе.
// дисплей нейм - аннотация(написано, что делает конкретно)

@ExtendWith(MockitoExtension.class)
public class TestCars {
    private static Connection h2Conn;

    @Mock
    Scanner mockScanner;
    @InjectMocks
    MainHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MainHandler(mockScanner);
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
    public void invalidVIN(){
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("VIN123")
                .thenReturn("1234567890qwertyut")
                .thenReturn("1234567890qwerty-")
                .thenReturn("0");
        tryDo(3, "VIN должен состоять из 17 знаков: цифры и латинские буквы");
    }
    @Test
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
        tryDo(3, "Год должен быть в диапазоне от 2000 до 2026");
    }
    @Test
    public void invalidPrice() {
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("2001")
                .thenReturn("-20")
                .thenReturn("sdfsdf")
                .thenReturn("0");
        tryDo(2, "Стоимость должна быть положительным числом");
    }
    @Test
    public void createSameCar() {
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1HGCM82633A123456")
                .thenReturn("Honda")
                .thenReturn("Accord")
                .thenReturn("2020")
                .thenReturn("30000")
                .thenReturn("0");

        tryDo(1, "Автомобиль с таким VIN уже существует");
    }
    @Test
    public void createCar() {
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("2001")
                .thenReturn("30000")
                .thenReturn("0");

        tryDo(1, "Автомобиль успешно добавлен!");
    }
    @Test
    void testFindCar(){
        when(mockScanner.nextLine())
                .thenReturn("1")      // Управление автомобилями
                .thenReturn("3")      // Поиск по критериям
                .thenReturn("1")      // Поиск по марке
                .thenReturn("Ford")   // Марка // VIN: 2FMDK3GC3DBA12345 Ford Focus, 2023 г. - 22000.500000 (доступен)
                .thenReturn("0");
        tryDo(1, "VIN: 2FMDK3GC3DBA12345 Ford Focus, 2023 г. - 22000.500000 (доступен)");

        when(mockScanner.nextLine())
                .thenReturn("1")      // Управление автомобилями
                .thenReturn("3")      // Поиск по критериям
                .thenReturn("2")      // Поиск по модели
                .thenReturn("Accord")  // Модель // VIN: 1HGCM82633A123456 Honda Accord, 2020 г. - 25000.000000 (доступен)
                .thenReturn("0");
        tryDo(1, "VIN: 1HGCM82633A123456 Honda Accord, 2020 г. - 25000.000000 (доступен)");

        when(mockScanner.nextLine())
                .thenReturn("1")      // Управление автомобилями
                .thenReturn("3")      // Поиск по критериям
                .thenReturn("3")      // Поиск по году
                .thenReturn("2025")   // Год // VIN: 3T1BF1FKXEU123456 Toyota Camry, 2025 г. - 32000.000000 (доступен)
                .thenReturn("0");     // Выход из программы
        tryDo(1, "VIN: 3T1BF1FKXEU123456 Toyota Camry, 2025 г. - 32000.000000 (доступен)");
    }



    private void tryDo(int cnt, String out){
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String ansAddCar = captureOutput(handler::mainMenu);
            assertEquals(cnt, countOccurrences(ansAddCar, out));
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
