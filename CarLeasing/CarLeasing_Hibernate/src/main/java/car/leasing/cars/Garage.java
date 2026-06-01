package car.leasing.cars;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.cars.domain.Car;
import car.leasing.exception.CarUnavailableException;
import car.leasing.exception.DeletionNotAllowedException;
import car.leasing.exception.InvalidParameterException;
import car.leasing.exception.ObjectNotFoundException;

@Service
public class Garage {
    private final CarRepository carRepository;
    public Garage(CarRepository carRepository){
        this.carRepository = carRepository;
    }

    public Car addCar(Car car) {
        if (carRepository.existsById(car.getVIN())) {
            throw new InvalidParameterException("Машина с VIN " + car.getVIN() + " уже существует");
        }
        return carRepository.save(car);
    }

    public List<Car> getCars(){
        return carRepository.findAll();
    }
    public Car getCarByVin(String VIN){
        if(!Car.checkVIN(VIN))
            throw new InvalidParameterException("VIN должен состоять из 17 знаков: цифры и латинские буквы");
        return carRepository.findById(VIN)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", VIN));
    }
    public List<Car> getCarsByBrand(String brand){
        if(brand.isBlank())
            throw new InvalidParameterException("Пустое значение марки недопустимо");
        return carRepository.findByBrand(brand);
    }
    public List<Car> getCarsByModel(String model) {
        if (model.isBlank())
            throw new InvalidParameterException("Пустое значение модели недопустимо");
        return carRepository.findByModel(model);
    }
    public List<Car> getCarsByYear(Integer year) {
        if (!Car.checkYear(year))
            throw new InvalidParameterException("Год должен быть в диапазоне от 2000 до 2026");
        return carRepository.findByYear(year);
    }

    @Transactional
    public void setStatus(String VIN, Car.Status status) {
        Car getCar = carRepository.findByIdForUpdate(VIN)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", VIN));
        if (getCar.getStatus() == Car.Status.InUse && status == Car.Status.InUse)
            throw new CarUnavailableException("");
        else {
            getCar.setStatus(status);//carRepository.updateStatus(VIN, status);
        }
    }

    @Transactional
    public Car deleteCar(String vin) {
        if (!Car.checkVIN(vin))
            throw new InvalidParameterException("VIN должен состоять из 17 знаков: цифры и латинские буквы");
        Car car = carRepository.findByIdForUpdate(vin)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", vin));
        if (car.getStatus() == Car.Status.Available)
            carRepository.deleteById(vin);
        else
            throw new DeletionNotAllowedException("Car", "VIN", vin);
        return car;
    }

    @Transactional
    public Car getCarByVinForUpdate(String VIN) {
        return carRepository.findByIdForUpdate(VIN)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", VIN));
    }
}