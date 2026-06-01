package car.leasing.integTest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import car.leasing.InitDB;
import car.leasing.MainHandler;
import car.leasing.cars.Garage;
import car.leasing.clients.Clients;
import car.leasing.cars.Car;
import car.leasing.clients.Client;
import car.leasing.contracts.Chancellery;
import car.leasing.contracts.ContractsDB;
import car.leasing.contracts.PaymentsDB;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestContracts {

    private static Connection h2Conn;
    private Car testCar;
    private Client testClient;

    @Mock
    Garage mockGarage;
    @Mock
    Clients mockClients;
    @Mock
    Scanner mockScanner;
    @InjectMocks
    MainHandler handler;

    @BeforeEach
    void setUp() {
        testCar = new Car("1234567890qwertyu", "BMW", "X5", "2020", new BigDecimal("50000"), Car.Status.Available);
        testClient = new Client(1, "Иванов", "1234567890", "+71234567890");

        when(mockGarage.getCarByVin()).thenReturn(testCar);
        when(mockGarage.setStatus(testCar, Car.Status.InUse)).thenReturn(true);
        when(mockClients.getClientByID()).thenReturn(testClient);
        when(mockClients.setClientStatus(testClient, true)).thenReturn(true);
    }

    @BeforeAll
    static void setUpH2() throws SQLException {
        h2Conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @Test
    void invalidMonths() {
        when(mockScanner.nextLine())
                .thenReturn("3")    // главное меню -> договоры
                .thenReturn("1")    // создать договор
                .thenReturn("1")    // выбор клиента по ID
                .thenReturn("afsdgh")
                .thenReturn("-345")
                .thenReturn("0");   // выход из главного меню

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(inv -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            try {Thread.sleep(1000);} catch (InterruptedException e) {throw new RuntimeException(e);}
            String output = captureOutput(handler::mainMenu);
            assertEquals(2, countOccurrences(output, "Введите положительное целое число"));
        }
    }
    @Test
    void invalidInitialPayment() {
        when(mockScanner.nextLine())
                .thenReturn("3")    // договоры
                .thenReturn("1")    // создать
                .thenReturn("1")    // выбор клиента по ID
                .thenReturn("12")   // корректные месяцы
                .thenReturn("-345")
                .thenReturn("sadfgh")
                .thenReturn("0");

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(inv -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String output = captureOutput(handler::mainMenu);
            assertEquals(2, countOccurrences(output, "Введите положительное число меньше стоимости машины: "));
        }
    }
    @Test
    void invalidRate() {
        when(mockScanner.nextLine())
                .thenReturn("3")
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("12")
                .thenReturn("10000")
                .thenReturn("200")       // некорректная ставка
                .thenReturn("-345")
                .thenReturn("sdfg")
                .thenReturn("0");        // выход

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(inv -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            String output = captureOutput(handler::mainMenu);
            assertEquals(3, countOccurrences(output, "Введите процентную ставку в виде числа от 0 до 100"));
        }
    }
    @Test
    void createContractSuccess() {
        try {
            Field chancelleryField = handler.getClass().getDeclaredField("chancellery");
            chancelleryField.setAccessible(true);
            Chancellery chancellery = (Chancellery) chancelleryField.get(handler);

            Field contractsDBField = chancellery.getClass().getDeclaredField("contractsDB");
            contractsDBField.setAccessible(true);
            ContractsDB contractsDB = new ContractsDB();
            contractsDBField.set(chancellery, contractsDB);

            PaymentsDB mockPaymentsDB = mock(PaymentsDB.class);
            when(mockPaymentsDB.createContractPayments(any())).thenReturn(true);

            Field paymentsDBField = contractsDB.getClass().getDeclaredField("paymentsDB");
            paymentsDBField.setAccessible(true);
            paymentsDBField.set(contractsDB, mockPaymentsDB);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        when(mockScanner.nextLine())
                .thenReturn("3")
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("12")
                .thenReturn("1000")
                .thenReturn("12")
                .thenReturn("0");

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(inv -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));

            String output = captureOutput(handler::mainMenu);
            assertEquals(1, countOccurrences(output, "Договор успешно добавлен!"));
        }
    }


    @AfterAll
    static void tearDownH2() throws SQLException {
        if (h2Conn != null) h2Conn.close();
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