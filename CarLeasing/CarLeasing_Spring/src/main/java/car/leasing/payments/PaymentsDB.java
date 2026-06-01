package car.leasing.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentsDB {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(PaymentsDB.class);

    private static final RowMapper<Payment> PAYMENT_ROW_MAPPER = (rs, rowNum) -> new Payment(
            rs.getLong("contract_id"),
            rs.getInt("payment_number"),
            rs.getBigDecimal("payment"),
            Payment.Status.fromString(rs.getString("status"))
    );

    public PaymentsDB(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveNewPayment(Payment payment) {
        String sql = "INSERT INTO payments (contract_id, payment_number, payment, status) VALUES (?, ?, ?, ?::payment_status)";
        jdbcTemplate.update(sql, payment.getContractId(), payment.getNumber(),
                payment.getPayment(), payment.getStatus().toString());
    }
    public void savePaymentsBatch(Long contractId, List<Payment> payments) {
        String sql = "INSERT INTO payments (contract_id, payment_number, payment, status) VALUES (?, ?, ?, ?::payment_status)";
        jdbcTemplate.batchUpdate(sql, payments, payments.size(),
                (PreparedStatement ps, Payment p) -> {
                    ps.setLong(1, contractId);
                    ps.setInt(2, p.getNumber());
                    ps.setBigDecimal(3, p.getPayment());
                    ps.setString(4, p.getStatus().toString());
                });
    }

    public List<Payment> getPaymentsByContractId(Long contractId) {
        String sql = "SELECT * FROM payments WHERE contract_id = ?";
        return jdbcTemplate.query(sql, PAYMENT_ROW_MAPPER, contractId);
    }
    public Optional<Payment> getCurrentPaymentByContract(Long contractId) {
        String sql = "SELECT * FROM payments WHERE contract_id = ? AND status = 'не оплачен' ORDER BY payment_number LIMIT 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, PAYMENT_ROW_MAPPER, contractId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean payCurrentPayment(Long contractId) {
        String sql = "UPDATE payments SET status = 'оплачен' WHERE contract_id = ? AND " +
                "payment_number = (SELECT min(payment_number) FROM payments WHERE contract_id = ? AND status = 'не оплачен')";
        return jdbcTemplate.update(sql, contractId, contractId) != 0;
    }
}
