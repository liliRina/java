package car.leasing.cars;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CarsDB {
    private final JdbcTemplate jdbcTemplate;
    public CarsDB(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Car> CAR_ROW_MAPPER = (rs, rowNum) -> {
        Car car = new Car(rs.getString("vin"),
                rs.getString("brand"),
                rs.getString("model"),
                 rs.getInt("year"),
                 rs.getBigDecimal("price"),
                Car.Status.fromString(rs.getString("status")));
        return car;
    };

    public Car saveNewCar(Car car) {
        String sql = "INSERT INTO cars (vin, brand, model, year, price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?::car_status) RETURNING *";
        return jdbcTemplate.queryForObject(sql, CAR_ROW_MAPPER, car.getVIN(), car.getBrand(), car.getModel(),
                car.getYear(), car.getPrice(), car.getStatus().toString());
    }

    public List<Car> getCars() {
        String sqlGetCars = "SELECT * FROM cars";
        return jdbcTemplate.query(sqlGetCars, CAR_ROW_MAPPER);
    }

    public Optional<Car> getCarByVIN(String VIN) {
        String sqlGetCarByVIN = "SELECT * FROM cars WHERE vin = ?";
        try {
            Car car = jdbcTemplate.queryForObject(sqlGetCarByVIN, CAR_ROW_MAPPER, VIN);
            return Optional.of(car);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    public Optional<Car> getCarByVINForUpdate(String VIN) {
        String sqlGetCarByVIN = "SELECT * FROM cars WHERE vin = ? FOR UPDATE";
        try {
            Car car = jdbcTemplate.queryForObject(sqlGetCarByVIN, CAR_ROW_MAPPER, VIN);
            return Optional.ofNullable(car);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Car> getCarsByBrand(String brand) {
        String sqlGetCarsByBrand = "SELECT * FROM cars WHERE brand = ?";
        return jdbcTemplate.query(sqlGetCarsByBrand, CAR_ROW_MAPPER, brand);
    }

    public List<Car> getCarsByModel(String model) {
        String sqlGetCarsByModel = "SELECT * FROM cars WHERE model = ?";
        return jdbcTemplate.query(sqlGetCarsByModel, CAR_ROW_MAPPER, model);
    }

    public List<Car> getCarsByYear(Integer year) {
        String sqlGetCarsByYear = "SELECT * FROM cars WHERE \"year\" = ?";
        return jdbcTemplate.query(sqlGetCarsByYear, CAR_ROW_MAPPER, year);
    }

    public boolean setStatusCars(String VIN, Car.Status status) {
        String sqlUpdateStatus = "UPDATE cars SET status = ?::car_status WHERE vin = ?";
        return jdbcTemplate.update(sqlUpdateStatus, status.toString(), VIN) != 0;
    }

    public void deleteCarByVIN(String VIN) {
        String sqlDeleteCarByVIN = "DELETE FROM cars WHERE VIN = ?";
        jdbcTemplate.update(sqlDeleteCarByVIN, VIN);
    }
}