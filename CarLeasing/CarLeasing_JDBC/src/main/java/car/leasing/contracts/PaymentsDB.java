package car.leasing.contracts;

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
                System.out.println("Ошибка сохранения платежей: " + e);
                connection.rollback();
                return false;
            }
        } catch (SQLException e){
            System.out.println("Ошибка сохранения платежей: " + e);
            e.printStackTrace();
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
            System.out.println("Не удалось откатить платежи: " + e);
            e.printStackTrace();
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
            System.out.println("Не удалось получить платежи: " + e);
            e.printStackTrace();
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
            else
                System.out.println("Текущий платёж не найден");
        } catch (SQLException e) {
            System.out.println("Текущий платёж не найден");
        }
        return null;
    }

    public boolean pay(LeasingContract contract) {
        Integer contractId = contract.getContractNumber();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlPayCurrentPayment)){
            statement.setInt(1, contractId);
            statement.setInt(2, contractId);

            if(statement.executeUpdate() == 0)
                System.out.println("Текущий платёж не найден");
            return true;
        } catch (SQLException e) {
            System.out.println("Не удалось оплатить платёж");
            return false;
        }
    }

    public void repay(LeasingContract contract) {
        Integer contractId = contract.getContractNumber();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlRepayCurrentPayment)){
            statement.setInt(1, contractId);
            statement.setInt(2, contractId);

            if(statement.executeUpdate() == 0)
                System.out.println("Текущий платёж не найден");
        } catch (SQLException e) {
            System.out.println("Не удалось откатить платёж");
        }
    }
}
