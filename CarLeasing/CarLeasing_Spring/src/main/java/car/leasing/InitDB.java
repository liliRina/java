package car.leasing;

import java.sql.*;

public class InitDB {
    private static String dbUrl = "jdbc:postgresql://localhost:5432/car_leasing";
    private static String dbUrlAdmin = "jdbc:postgresql://localhost:5432/postgres";
    private static final String user = "postgres";
    private static final String password = "123456";
    InitDB(){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String sqlCreateDB = "CREATE DATABASE car_leasing" +
                " WITH ENCODING = 'UTF8' " +
                " OWNER = " +  user  +
                " TEMPLATE = template0" +
                " LC_COLLATE = 'en_US.UTF-8'" +
                " LC_CTYPE = 'en_US.UTF-8'" +
                " CONNECTION LIMIT = -1";

        String sqlCheckExistCarS = " SELECT 1 " +
                "FROM pg_type " +
                "WHERE pg_type.typname = 'car_status'";
        String sqlCreateCarStatus = "CREATE TYPE car_status AS ENUM ('доступен', 'в лизинге')";
        String sqlCreateCarsTable =
                "CREATE TABLE IF NOT EXISTS cars (" +
                "VIN VARCHAR(17) PRIMARY KEY, " +
                "brand TEXT NOT NULL, " +
                "model TEXT NOT NULL, " +
                "year SMALLINT NOT NULL CHECK (year BETWEEN 2000 AND 2026), " +
                "price NUMERIC(15, 6), " +
                "status car_status DEFAULT 'доступен')";

        String sqlCheckExistClientS = " SELECT 1 " +
                "FROM pg_type " +
                "WHERE pg_type.typname = 'client_status'";
        String sqlCreateClientStatus = "CREATE TYPE client_status AS ENUM ('есть активный договор', 'нет активных договоров')";
        String sqlCreateClientsTable =
                "CREATE TABLE IF NOT EXISTS clients ( " +
                "id BIGSERIAL PRIMARY KEY, " +
                "full_name TEXT NOT NULL, " +
                "passport_number VARCHAR(10) UNIQUE NOT NULL CHECK (passport_number ~ '^\\d{10}$'), " +
                "phone_number VARCHAR(12) UNIQUE NOT NULL CHECK (phone_number ~ '^\\+7\\d{10}$'), " +
                "status client_status DEFAULT 'нет активных договоров')";

        String sqlCheckExistPaymentS = " SELECT 1 " +
                "FROM pg_type " +
                "WHERE pg_type.typname = 'payment_status'";
        String sqlCreatePaymentStatus = "CREATE TYPE payment_status AS ENUM ('оплачен', 'не оплачен');";
        String sqlCreatePaymentsTable =
                "CREATE TABLE IF NOT EXISTS payments ( " +
                "contract_id BIGINT NOT NULL, " +
                "payment_number INT NOT NULL, " +
                "payment NUMERIC(15, 6) NOT NULL, " +
                "status payment_status DEFAULT 'не оплачен', "+
                "PRIMARY KEY (contract_id, payment_number))";
        String sqlCheckExistDB = " SELECT 1 " +
                                "FROM pg_database " +
                                "WHERE datname = 'car_leasing'";

        try (Connection conn = DriverManager.getConnection(dbUrlAdmin, user, password);
                Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlCheckExistDB);
            if (!resultSet.next()) {
                statement.executeUpdate(sqlCreateDB);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при создании баз данных: car_leasing");
            e.printStackTrace();
        }


        try(Connection connection = DriverManager.getConnection(dbUrl, user, password);
            Statement statement = connection.createStatement()){

            ResultSet resultSet = statement.executeQuery(sqlCheckExistCarS);
            if(!resultSet.next())
                statement.executeUpdate(sqlCreateCarStatus);
            statement.executeUpdate(sqlCreateCarsTable);

            resultSet = statement.executeQuery(sqlCheckExistClientS);
            if(!resultSet.next())
                statement.executeUpdate(sqlCreateClientStatus);
            statement.executeUpdate(sqlCreateClientsTable);

            resultSet = statement.executeQuery(sqlCheckExistPaymentS);
            if(!resultSet.next())
                statement.executeUpdate(sqlCreatePaymentStatus);
            statement.executeUpdate(sqlCreatePaymentsTable);
        } catch (SQLException e) {
            System.out.println("Ошибка при создании баз данных: car_leasing");
            e.printStackTrace();
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, user, password);
    }
}
