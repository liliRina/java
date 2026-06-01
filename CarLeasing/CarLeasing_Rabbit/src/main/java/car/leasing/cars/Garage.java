package car.leasing.cars;

import java.util.List;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.cars.domain.Car;
import car.leasing.cars.domain.CarDTO;
import car.leasing.exception.CarUnavailableException;
import car.leasing.exception.DeletionNotAllowedException;
import car.leasing.exception.InvalidParameterException;
import car.leasing.exception.ObjectNotFoundException;

@Service
public class Garage {
    private final CarRepository carRepository;
    private final RabbitTemplate rabbitTemplate;
    public Garage(CarRepository carRepository, RabbitTemplate rabbitTemplate){
        this.carRepository = carRepository;
        this.rabbitTemplate = rabbitTemplate;
    }


    public void addCar(CarDTO carDTO) {
        if (carRepository.existsById(carDTO.getVIN()))
            throw new InvalidParameterException("Машина с VIN " + carDTO.getVIN() + " уже существует");

        rabbitTemplate.convertAndSend("car.ex", "car.add", carDTO,
                new CorrelationData("cохранение авто с VIN = " + carDTO.getVIN()));
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

    public Boolean setStatus(String VIN, Car.Status status) {
        Car newCar = carRepository.findById(VIN)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", VIN));
        if (newCar.getStatus() == Car.Status.InUse && status == Car.Status.InUse)
            throw new CarUnavailableException("");
        else
            return (Boolean) rabbitTemplate.convertSendAndReceive("car.ex", "car.set_status", newCar,
                    new CorrelationData("изменение статуса авто с VIN = " + VIN + " на " + status));
    }

    public void deleteCar(String vin) {
        if (!Car.checkVIN(vin))
            throw new InvalidParameterException("VIN должен состоять из 17 знаков: цифры и латинские буквы");
        Car car = carRepository.findById(vin)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", vin));
        if (car.getStatus() == Car.Status.Available)
            rabbitTemplate.convertAndSend("car.ex", "car.delete", vin,
                    new CorrelationData("удаление авто с VIN = " + vin));
        else
            throw new DeletionNotAllowedException("Car", "VIN", vin);
    }

    @Transactional
    public Car getCarByVinForUpdate(String VIN) {
        return carRepository.findByIdForUpdate(VIN)
                .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", VIN));
    }
}