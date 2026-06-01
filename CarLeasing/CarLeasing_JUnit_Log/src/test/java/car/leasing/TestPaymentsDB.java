package car.leasing;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import car.leasing.cars.Car;
import car.leasing.clients.Client;
import car.leasing.contracts.LeasingContract;
import car.leasing.contracts.PaymentsDB;

import java.math.BigDecimal;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TestPaymentsDB {
    private static Connection h2Conn;
    private Car testCar;
    private Client testClient;
    private LeasingContract contract;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        testCar = new Car("1234567890qwertyu", "BMW", "X5", "2020", new BigDecimal("50000"), Car.Status.Available);
        testClient = new Client(1, "Иванов", "1234567890", "+71234567890");
        contract = new LeasingContract(1, testCar, testClient, 2, BigDecimal.valueOf(10000), 15.5);
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));
            PaymentsDB paymentsDB = new PaymentsDB();
            paymentsDB.createContractPayments(contract);
        }
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
                    "status payment_status DEFAULT 'не оплачен', " +
                    "PRIMARY KEY (contract_id, payment_number))");
        }
    }

    @Test
    @DisplayName("Оплата платежа")
    public void testPayCurrentPayment() throws SQLException {
        // Оплачиваем первый платёж
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));

            PaymentsDB paymentsDB = new PaymentsDB();
            boolean paid = paymentsDB.pay(contract);
            assertTrue(paid);
            // Проверяем, что первый платёж стал оплачен
            try (PreparedStatement stmt = h2Conn.prepareStatement("SELECT status FROM payments WHERE contract_id = 1 AND payment_number = 1")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                assertEquals("оплачен", rs.getString("status"));
            }
            // Второй платёж остался не оплачен
            try (PreparedStatement stmt = h2Conn.prepareStatement("SELECT status FROM payments WHERE contract_id = 1 AND payment_number = 2")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                assertEquals("не оплачен", rs.getString("status"));
            }
        }
    }
    @Test
    @DisplayName("Возврат оплаты платежа")
    public void testRepay() throws SQLException {
        try (MockedStatic<InitDB> mocked = mockStatic(InitDB.class)) {
            mocked.when(InitDB::getConnection).thenAnswer(invocation -> DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", ""));

            PaymentsDB paymentsDB = new PaymentsDB();
            paymentsDB.pay(contract);

            try (PreparedStatement stmt = h2Conn.prepareStatement("SELECT COUNT (*) FROM payments WHERE contract_id = 1 AND status = 'не оплачен'::payment_status")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                assertEquals(1, rs.getInt(1));
            }
            paymentsDB = new PaymentsDB();
            paymentsDB.repay(contract);

            try (PreparedStatement stmt = h2Conn.prepareStatement("SELECT COUNT (*) FROM payments WHERE contract_id = 1 AND status = 'не оплачен'::payment_status")) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                assertEquals(2, rs.getInt(1));
            }
        }
    }
    @AfterAll
    static void tearDownH2() throws SQLException {
        if (h2Conn != null) h2Conn.close();
    }
}
