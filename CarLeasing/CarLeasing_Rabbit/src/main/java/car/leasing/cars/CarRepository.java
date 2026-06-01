package car.leasing.cars;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import car.leasing.cars.domain.Car;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, String> {

    List<Car> findByBrand(String brand);
    List<Car> findByModel(String model);
    List<Car> findByYear(Integer year);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Car c WHERE c.VIN = :vin")
    Optional<Car> findByIdForUpdate(@Param("vin") String vin);

    @Modifying
    @Query("UPDATE Car c SET c.status = :status WHERE c.VIN = :vin")
    void updateStatus(@Param("vin") String vin, @Param("status") Car.Status status);
}