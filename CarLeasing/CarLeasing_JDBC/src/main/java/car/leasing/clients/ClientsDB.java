package car.leasing.clients;

import car.leasing.InitDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientsDB {
    private final String sqlInsertClient = "INSERT INTO clients (full_name, passport_number, phone_number) " +
                        "VALUES (?, ?, ?)";
    private final String sqlGetClients = "SELECT * FROM clients";
    private final String sqlGetClientById = "SELECT * FROM clients WHERE id = ?";
    private final String sqlGetClientByFullName = "SELECT * FROM clients WHERE full_name = ?";
    private final String sqlGetClientByPassportNumber = "SELECT * FROM clients WHERE passport_number = ?";
    private final String sqlGetClientByPhoneNumber = "SELECT * FROM clients WHERE phone_number = ?";

    private final String sqlGetClientByPassportUpdate = "SELECT * FROM clients WHERE passport_number = ? FOR UPDATE";
    private final String sqlUpdateStatus = "UPDATE clients SET status = ?::client_status WHERE passport_number = ?";
    private final String sqlDeleteClientByPassport = "DELETE FROM clients WHERE passport_number = ?";


    public ClientsDB(){

    }
    public void saveNewClient(Client client){
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlInsertClient)) {
            statement.setString(1, client.getFullName());
            statement.setString(2, client.getPassportNumber());
            statement.setString(3, client.getPhoneNumber());
            statement.executeUpdate();
            System.out.println("Клиент успешно добавлен!");
        } catch (SQLException e) {
            System.out.println("Ошибка сохранения клиента: " + e);
            e.printStackTrace();
        }
    }
    public List<Client> getClients(){
        List<Client> clients = new ArrayList<>();
        try(Connection connection = InitDB.getConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlGetClients);
            while(resultSet.next())
                clients.add(readClient(resultSet));

        } catch (SQLException e) {
            System.out.println("Не удалось получить клиентов: " + e);
            e.printStackTrace();
        }
        return clients;
    }

    public Client getClientById(Integer id){
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlGetClientById)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return readClient(resultSet);

        } catch (SQLException e) {
            System.out.println("Не удалось получить клиента: " + e);
            e.printStackTrace();
        }
        return null;
    }

    public List<Client> getClientsByFullName(String fullName){
        List<Client> clients = new ArrayList<>();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlGetClientByFullName)) {

            statement.setString(1, fullName);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next())
                clients.add(readClient(resultSet));

        } catch (SQLException e) {
            System.out.println("Не удалось получить клиентов: " + e);
            e.printStackTrace();
        }
        return clients;
    }

    public Client getClientByPassportNumber(String passportNumber){
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlGetClientByPassportNumber)) {

            statement.setString(1, passportNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return readClient(resultSet);

        } catch (SQLException e) {
            System.out.println("Не удалось получить клиента: " + e);
            e.printStackTrace();
        }
        return null;
    }

    public Client getClientByPhoneNumber(String phoneNumber){
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlGetClientByPhoneNumber)) {

            statement.setString(1, phoneNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return readClient(resultSet);
        } catch (SQLException e) {
            System.out.println("Не удалось получить клиента: " + e);
            e.printStackTrace();
        }
        return null;
    }

    private Client readClient(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt(1);
        String fullName = resultSet.getString(2);
        String passportNumber = resultSet.getString(3);
        String phoneNumber = resultSet.getString(4);
        return new Client(id, fullName, passportNumber, phoneNumber);
    }

    public boolean setClientStatus(Client client, boolean doWithContract) {
        String passportNumber = client.getPassportNumber();
        try(Connection connection = InitDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sqlGetClientByPassportUpdate);
            PreparedStatement statementUpdate = connection.prepareStatement(sqlUpdateStatus)){
            connection.setAutoCommit(false);

            statement.setString(1, passportNumber);
            statementUpdate.setString(2, passportNumber);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                if (doWithContract) {
                    statementUpdate.setObject(1, "есть активный договор");
                    statementUpdate.executeUpdate();
                } else {
                    statementUpdate.setObject(1, "нет активных договоров");
                    statementUpdate.executeUpdate();
                }
            }
            else {
                System.out.println("Клиент не найден ");
                return false;
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Клиент не найден " + e);
            e.printStackTrace();
            return false;
        }
    }

    public void deleteClient(String passportNumber) {
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlGetClientByPassportUpdate);
             PreparedStatement statementUpdate = connection.prepareStatement(sqlDeleteClientByPassport)) {
            connection.setAutoCommit(false);

            statement.setString(1, passportNumber);
            statementUpdate.setString(1, passportNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                if (!resultSet.getString("status").equals("есть активный договор")) {
                    System.out.println("Клиент успешно удалён!");
                    statementUpdate.executeUpdate();
                }
                else {
                    System.out.println("Есть активный договор у клиента");
                }
            }
            else
                System.out.println("Клиент не найден");
            connection.commit();
        } catch (SQLException e) {
            System.out.println("Не удалось удалить клиента: " + e);
            e.printStackTrace();
        }
    }
}
