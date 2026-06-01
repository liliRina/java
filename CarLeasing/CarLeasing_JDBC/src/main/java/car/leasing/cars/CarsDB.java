package car.leasing.cars;

import car.leasing.InitDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarsDB {
    private final String sqlInsertCar = "INSERT INTO cars (VIN, brand, model, \"year\", price, status) " +
            "VALUES (?, ?, ?, ?, ?, ?::car_status)";

    private final String sqlGetCars = "SELECT * FROM cars";
    private final String sqlGetCarByVIN = "SELECT * FROM cars WHERE VIN = ?";
    private final String sqlGetCarsByBrand = "SELECT * FROM cars WHERE brand = ?";
    private final String sqlGetCarsByModel = "SELECT * FROM cars WHERE model = ?";
    private final String sqlGetCarsByYear = "SELECT * FROM cars WHERE year = ?";

    private final String sqlGetCarByVINUpdate = "SELECT * FROM cars WHERE VIN = ? FOR UPDATE";
    private final String sqlUpdateStatus = "UPDATE cars SET status = ?::car_status WHERE vin = ?";
    private final String sqlDeleteCarByVIN = "DELETE FROM cars WHERE VIN = ?";

    public CarsDB() {

    }

    public void saveNewCar(Car car) {
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlInsertCar)) {
            statement.setString(1, car.getVIN());
            statement.setString(2, car.getBrand());
            statement.setString(3, car.getModel());
            statement.setInt(4, Integer.valueOf(car.getYear()));
            statement.setBigDecimal(5, car.getPrice());
            statement.setObject(6, car.getStatus().toString());
            statement.executeUpdate();
            System.out.println("Автомобиль успешно добавлен!");
        } catch (SQLException e) {
            System.out.println("Ошибка сохранения автомобиля: " + e);
            e.printStackTrace();
        }
    }

    public List<Car> getCars() {
        List<Car> cars = new ArrayList<>();
        try (Connection connection = InitDB.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlGetCars);
            while (resultSet.next())
                cars.add(readCar(resultSet));

        } catch (SQLException e) {
            System.out.println("Не удалось получить машины: " + e);
            e.printStackTrace();
        }
        return cars;
    }

    public List<Car> getCarsByBrand(String brand) {
        List<Car> cars = new ArrayList<>();
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlGetCarsByBrand)) {

            statement.setString(1, brand);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
                cars.add(readCar(resultSet));

        } catch (SQLException e) {
            System.out.println("Не удалось получить машины: " + e);
            e.printStackTrace();
        }
        return cars;
    }

    public List<Car> getCarsByModel(String model) {
        List<Car> cars = new ArrayList<>();
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlGetCarsByModel)) {

            statement.setString(1, model);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
                cars.add(readCar(resultSet));

        } catch (SQLException e) {
            System.out.println("Не удалось получить машины: " + e);
            e.printStackTrace();
        }
        return cars;
    }

    public List<Car> getCarsByYear(String year) {
        List<Car> cars = new ArrayList<>();
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlGetCarsByYear)) {

            statement.setInt(1, Integer.valueOf(year));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
                cars.add(readCar(resultSet));

        } catch (SQLException e) {
            System.out.println("Не удалось получить машины: " + e);
            e.printStackTrace();
        }
        return cars;
    }

    public Car getCarByVIN(String VIN) {
        Car car = null;
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlGetCarByVIN)) {

            statement.setString(1, VIN);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                return readCar(resultSet);

        } catch (SQLException e) {
            System.out.println("Не удалось получить машины: " + e);
            e.printStackTrace();
        }
        return car;
    }

    public Car readCar(ResultSet resultSet) throws SQLException {
        String VIN = resultSet.getString(1);
        String brand = resultSet.getString(2);
        String model = resultSet.getString(3);
        Integer year = resultSet.getInt(4);
        BigDecimal price = resultSet.getBigDecimal(5);
        Car.Status status = Car.Status.fromString(resultSet.getString(6));

        return new Car(VIN, brand, model, year.toString(), price, status);
    }

    public boolean setStatusCars(Car car, Car.Status status) {
        String VIN = car.getVIN();
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlGetCarByVINUpdate);
             PreparedStatement statementUpdate = connection.prepareStatement(sqlUpdateStatus)) {
            connection.setAutoCommit(false);

            Car carTable;
            statement.setString(1, VIN);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                carTable = readCar(resultSet);
            else {
                System.out.println("Машина не найдена");
                return false;
            }
            if (carTable.getStatus() == Car.Status.InUse && status == Car.Status.InUse) {
                System.out.println("Машину только что заняли. Попробуйте ещё раз");
                return false;
            }
            statementUpdate.setObject(1, status.toString());
            statementUpdate.setString(2, VIN);
            statementUpdate.executeUpdate();
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Не удалось обновить статус машины: " + e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCarByVIN(String VIN) {
        try (Connection connection = InitDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlGetCarByVINUpdate);
             PreparedStatement statementUpdate = connection.prepareStatement(sqlDeleteCarByVIN)) {
            connection.setAutoCommit(false);

            Car carTable;
            statement.setString(1, VIN);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
                carTable = readCar(resultSet);
            else {
                System.out.println("Машина не найдена");
                return false;
            }
            if (carTable.getStatus() == Car.Status.InUse) {
                System.out.println("Есть активный договор по машине");
                return false;
            }
            else {
                statementUpdate.setString(1, VIN);
                if (statementUpdate.executeUpdate() != 0)
                    System.out.println("Машина успешно удалена");
                connection.commit();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Не удалось удалить машину: " + e);
            e.printStackTrace();
            return false;
        }
    }
}