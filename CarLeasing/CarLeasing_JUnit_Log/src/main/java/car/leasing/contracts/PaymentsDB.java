package car.leasing.contracts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import car.leasing.InitDB;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentsDB {
    private final String sqlInsertPayment = "INSERT INTO payments (contract_id, payment_number, payment, status) " +
            "VALUES (?, ?, ?, ?::payment_status)";

    private final String sqlGetPaymentByContractId = "SELECT * FROM payments WHERE contract_id = ?";
    private final String sqlGetCurrentByContractId = "SELECT * FROM payments WHERE contract_id = ? " +
            "AND status = 'не оплачен'::payment_status ORDER BY payment_number LIMIT 1";

    private final String sqlPayCurrentPayment = "UPDATE payments SET status = 'оплачен'::payment_status WHERE contract_id = ? AND " +
            "payment_number = (SELECT min(payment_number) FROM payments WHERE contract_id = ? AND status = 'не оплачен')";
    private final String sqlRepayCurrentPayment = "UPDATE payments SET status = 'не оплачен'::payment_status WHERE contract_id = ? AND " +
            "payment_number = (SELECT max(payment_number) FROM payments WHERE contract_id = ? AND status = 'оплачен')";

    private final String sqlDeleteByContractId = "DELETE FROM payments WHERE contract_id = ?";

    private static final Logger log = LoggerFactory.getLogger(PaymentsDB.class);


    public boolean createContractPayments(LeasingContract contract){
        BigDecimal loan = contract.getCar().getPrice().subtract(contract.getInitialPayment());

        BigDecimal monthRate = BigDecimal.valueOf(contract.getRate())
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal payment = loan.multiply(monthRate)
                .multiply(monthRate.add(BigDecimal.ONE).pow(contract.getMonthsCnt()))
                .divide(monthRate.add(BigDecimal.ONE).pow(contract.getMonthsCnt())
                        .subtract(BigDecimal.ONE), 6, RoundingMode.HALF_UP);

        Integer contractId = contract.getContractNumber();
        try (Connection connection = InitDB.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for(int i = 1; i <= contract.getMonthsCnt(); i++){
                   Payment newPayment = new Payment(contractId, i, payment);
                   saveNewPayment(newPayment, connection);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Ошибка сохранения платежей по договору №{}: ",contractId,  e);
                throw e;
            }
        } catch (SQLException e){
            log.error("Ошибка сохранения платежей по договору №{}: ",contractId,  e);
            return false;
        }
        return true;
    }
    public void deleteContractPayments(LeasingContract contract) {
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlDeleteByContractId)) {

            statement.setInt(1, contract.getContractNumber());
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Не удалось откатить платежи по договору №{}: ",contract.getContractNumber(), e);
        }
    }

    public void saveNewPayment(Payment payment, Connection connection) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement(sqlInsertPayment)) {
            statement.setInt(1, payment.getContractId());
            statement.setInt(2, payment.getNumber());
            statement.setBigDecimal(3, payment.getPayment());
            statement.setObject(4, payment.getStatus().toString());
            statement.executeUpdate();
        }
    }

    public List<Payment> getPaymentsByContractId(LeasingContract contract) {
        List<Payment> payments = new ArrayList<>();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlGetPaymentByContractId)) {

            statement.setInt(1, contract.getContractNumber());
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next())
                payments.add(readPayment(resultSet));

        } catch (SQLException e) {
            log.error("Ошибка при получении платежей по договору №{}: ", contract.getContractNumber(), e);
        }
        return payments;
    }

    private Payment readPayment(ResultSet resultSet) throws SQLException {
        Integer contractId = resultSet.getInt(1);
        Integer paymentNumber = resultSet.getInt(2);
        BigDecimal payment = resultSet.getBigDecimal(3);
        Payment.Status status = Payment.Status.fromString(resultSet.getString(4));
        return new Payment(contractId, paymentNumber, payment, status);
    }

    public Payment getCurrentPaymentByContract(LeasingContract contract) {
        Integer contractId = contract.getContractNumber();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlGetCurrentByContractId)){
            statement.setInt(1, contractId);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next())
                return readPayment(resultSet);
        } catch (SQLException e) {
            log.error("Ошибка при получении текущего платежа по договору №{}: ", contract.getContractNumber(), e);
        }
        return null;
    }

    public boolean pay(LeasingContract contract) {
        Integer contractId = contract.getContractNumber();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlPayCurrentPayment)){
            statement.setInt(1, contractId);
            statement.setInt(2, contractId);

            if(statement.executeUpdate() != 0)
                return true;
        } catch (SQLException e) {
            log.error("Ошибка при оплате платежа по договору №{}: ", contractId, e);
            return false;
        }
        return false;
    }

    public void repay(LeasingContract contract) {
        Integer contractId = contract.getContractNumber();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlRepayCurrentPayment)){
            statement.setInt(1, contractId);
            statement.setInt(2, contractId);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Ошибка при откате платежа по договору №{}: ", contractId, e);
        }
    }
}
