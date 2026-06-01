package car.leasing;

import car.leasing.contracts.LeasingContract;
import car.leasing.contracts.PaymentsDB;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import car.leasing.cars.Car;
import car.leasing.clients.Client;
import car.leasing.contracts.Payment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestContracts {

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
            String sqlCreatePaymentStatus = "CREATE TYPE payment_status AS ENUM ('оплачен', 'не оплачен');";
            stmt.execute(sqlCreatePaymentStatus);
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (" +
                    "contract_id INT NOT NULL, " +
                    "payment_number INT NOT NULL, " +
                    "payment DECIMAL(15,6), " +
                    "status payment_status Default 'не оплачен', " +
                    "PRIMARY KEY (contract_id, payment_number))");
        }
    }



    private LeasingContract createDummyContract(int contractId) {
        Car car = new Car("VIN123", "BMW", "X5", "2020", new BigDecimal("50000"), Car.Status.Available);
        Client client = new Client(1, "Иванов Иван", "1234567890", "+71234567890");
        return new LeasingContract(contractId, car, client, 3, new BigDecimal("10000"), 12.0);
    }

    @Test
    void testCreateContractPayments() throws SQLException {

        LeasingContract contract = createDummyContract(100);


        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation ->
                    DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));

            PaymentsDB paymentsDB = new PaymentsDB(); // используем реальный объект
            boolean created = paymentsDB.createContractPayments(contract);
            assertTrue(created);

            // Проверяем, что в таблице появилось 3 записи
            try (Statement stmt = h2Conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM payments WHERE contract_id = 100")) {
                rs.next();
                assertEquals(3, rs.getInt(1));
            }
        }
    }

    @Test
    void testPayCurrentPayment() throws SQLException {
        // Сначала создаём платежи
        LeasingContract contract = createDummyContract(101);
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation ->
                    DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            PaymentsDB paymentsDB = new PaymentsDB();
            paymentsDB.createContractPayments(contract);
        }

        // Оплачиваем первый платёж
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation ->
                    DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            PaymentsDB paymentsDB = new PaymentsDB();
            boolean paid = paymentsDB.pay(contract);
            assertTrue(paid);

            // Проверяем, что первый платёж стал оплачен
            try (PreparedStatement stmt = h2Conn.prepareStatement(
                    "SELECT status FROM payments WHERE contract_id = 101 AND payment_number = 1")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                assertEquals("оплачен", rs.getString("status"));
            }
            // Второй платёж остался не оплачен
            try (PreparedStatement stmt = h2Conn.prepareStatement(
                    "SELECT status FROM payments WHERE contract_id = 101 AND payment_number = 2")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                assertEquals("не оплачен", rs.getString("status"));
            }
        }
    }

    @Test
    void testGetCurrentPayment() throws SQLException {
        // Создаём три платежа, первый и второй помечаем оплаченными
        try (PreparedStatement stmt = h2Conn.prepareStatement(
                "INSERT INTO payments (contract_id, payment_number, payment, status) VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, 102);
            stmt.setInt(2, 1);
            stmt.setBigDecimal(3, new BigDecimal("1000"));
            stmt.setString(4, "оплачен");
            stmt.executeUpdate();

            stmt.setInt(1, 102);
            stmt.setInt(2, 2);
            stmt.setBigDecimal(3, new BigDecimal("1000"));
            stmt.setString(4, "оплачен");
            stmt.executeUpdate();

            stmt.setInt(1, 102);
            stmt.setInt(2, 3);
            stmt.setBigDecimal(3, new BigDecimal("1000"));
            stmt.setString(4, "не оплачен");
            stmt.executeUpdate();
        }

        LeasingContract contract = createDummyContract(102);
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation ->
                    DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            PaymentsDB paymentsDB = new PaymentsDB();
            Payment current = paymentsDB.getCurrentPaymentByContract(contract);
            assertNotNull(current);
            assertEquals(3, current.getNumber());
            assertEquals("не оплачен", current.getStatus());
        }
    }

    @Test
    void testInvalidMonthsInput() {
        // Имитируем ввод: выбираем меню договоров, затем создание договора, и вводим некорректные месяцы
        when(mockScanner.nextLine())
                .thenReturn("3")            // главное меню -> договоры
                .thenReturn("1")            // создать договор
                .thenReturn("1")            // выбор клиента по ID (для простоты)
                .thenReturn("1")            // ID клиента = 1
                .thenReturn("VIN123")       // VIN автомобиля
                .thenReturn("0")            // некорректное количество месяцев (0)
                .thenReturn("5")            // корректное количество месяцев
                .thenReturn("10000")        // первоначальный взнос
                .thenReturn("12.5")         // процентная ставка
                .thenReturn("0")            // выход
                .thenReturn("0");           // выход из меню договоров

        when(mockScanner.nextBigDecimal())
                .thenReturn(new BigDecimal("10000"))
                .thenReturn(BigDecimal.ZERO); // dummy

        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation ->
                    DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));

            String output = captureOutput(() -> handler.mainMenu());

            // Должно быть сообщение о том, что количество месяцев должно быть больше 0
            assertTrue(countOccurrences(output, "Количество месяцев должно быть положительным") >= 1);
        }
    }
}
