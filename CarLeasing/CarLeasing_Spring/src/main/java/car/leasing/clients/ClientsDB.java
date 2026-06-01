package car.leasing.clients;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClientsDB {
    private final JdbcTemplate jdbcTemplate;
    public ClientsDB(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Client> CLIENT_ROW_MAPPER = (rs, rowNum) -> new Client(
            rs.getLong("id"),
            rs.getString("full_name"),
            rs.getString("passport_number"),
            rs.getString("phone_number"),
            Client.Status.fromString(rs.getString("status"))
    );

    public Client saveNewClient(Client client) {
        String sql = "INSERT INTO clients (full_name, passport_number, phone_number) " +
                "VALUES (?, ?, ?) RETURNING *";
        return jdbcTemplate.queryForObject(sql, CLIENT_ROW_MAPPER, client.getFullName(), client.getPassportNumber(), client.getPhoneNumber());
    }

    public List<Client> getClients() {
        String sql = "SELECT * FROM clients";
        return jdbcTemplate.query(sql, CLIENT_ROW_MAPPER);
    }

    public Optional<Client> getClientById(Long id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, CLIENT_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    public List<Client> getClientsByFullName(String fullName) {
        String sql = "SELECT * FROM clients WHERE full_name = ?";
        return jdbcTemplate.query(sql, CLIENT_ROW_MAPPER, fullName);
    }
    public Optional<Client> getClientByPassportNumber(String passportNumber) {
        String sql = "SELECT * FROM clients WHERE passport_number = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, CLIENT_ROW_MAPPER, passportNumber));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    public Optional<Client> getClientByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM clients WHERE phone_number = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, CLIENT_ROW_MAPPER, phoneNumber));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Client> getClientByPassportForUpdate(String passportNumber) {
        String sql = "SELECT * FROM clients WHERE passport_number = ? FOR UPDATE";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, CLIENT_ROW_MAPPER, passportNumber));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void updateClientStatus(String passportNumber, Client.Status status) {
        String sql = "UPDATE clients SET status = ?::client_status WHERE passport_number = ?";
        jdbcTemplate.update(sql, status.toString(), passportNumber);
    }

    public void deleteClient(String passportNumber) {
        String sql = "DELETE FROM clients WHERE passport_number = ? AND status != ?::client_status";
        jdbcTemplate.update(sql, passportNumber, Client.Status.HasActiveContract.toString());
    }
}
