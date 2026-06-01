package car.leasing.cars;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.exception.CarUnavailableException;
import car.leasing.exception.DeletionNotAllowedException;
import car.leasing.exception.InvalidParameterException;
import car.leasing.exception.ObjectNotFoundException;

@Service
public class Garage {
    private final CarsDB carsDB;
    public Garage(CarsDB carsDB){
        this.carsDB = carsDB;
    }

    public Car addCar(Car car) {
        return carsDB.saveNewCar(car);
    }

    public List<Car> getCars(){
        return carsDB.getCars();
    }
    public Car getCarByVin(String VIN){
        if(!Car.checkVIN(VIN))
            throw new InvalidParameterException("VIN должен состоять из 17 знаков: цифры и латинские буквы");
        return carsDB.getCarByVIN(VIN)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", VIN));
    }
    public List<Car> getCarsByBrand(String brand){
        if(brand.isBlank())
            throw new InvalidParameterException("Пустое значение марки недопустимо");
        return carsDB.getCarsByBrand(brand);
    }
    public List<Car> getCarsByModel(String model) {
        if (model.isBlank())
            throw new InvalidParameterException("Пустое значение модели недопустимо");
        return carsDB.getCarsByModel(model);
    }
    public List<Car> getCarsByYear(Integer year) {
        if (!Car.checkYear(year))
            throw new InvalidParameterException("Год должен быть в диапазоне от 2000 до 2026");
        return carsDB.getCarsByYear(year);
    }

    @Transactional
    public void setStatus(String VIN, Car.Status status) {
        Car getCar = carsDB.getCarByVINForUpdate(VIN)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", VIN));
        if (getCar.getStatus() == Car.Status.InUse && status == Car.Status.InUse)
            throw new CarUnavailableException("");
        else {
            carsDB.setStatusCars(VIN, status);
        }
    }

    @Transactional
    public Car deleteCar(String vin) {
        if (!Car.checkVIN(vin))
            throw new InvalidParameterException("VIN должен состоять из 17 знаков: цифры и латинские буквы");
        Car car = carsDB.getCarByVINForUpdate(vin)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", vin));
        if (car.getStatus() == Car.Status.Available)
            carsDB.deleteCarByVIN(vin);
        else
            throw new DeletionNotAllowedException("Car", "VIN", vin);
        return car;
    }
}