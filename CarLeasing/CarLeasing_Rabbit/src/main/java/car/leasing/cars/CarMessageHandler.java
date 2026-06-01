package car.leasing.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.cars.domain.Car;
import car.leasing.cars.domain.CarDTO;
import car.leasing.exception.ObjectNotFoundException;

@Component
@RabbitListener(queues = "carQueue")
public class CarMessageHandler {
    public final CarRepository carRepository;
    private static final Logger log = LoggerFactory.getLogger(CarMessageHandler.class);

    CarMessageHandler(CarRepository carRepository){
        this.carRepository = carRepository;
    }

    @RabbitHandler
    public void handleAdd(CarDTO carDTO){
        Car car = new Car (carDTO.getVIN(), carDTO.getBrand(), carDTO.getModel(),
            carDTO.getYear(), carDTO.getPrice(), Car.Status.Available);
        try{
            carRepository.save(car);
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при сохранении автомобиля: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitHandler
    @Transactional
    @SendTo("")
    public Boolean handleSetStatus(Car updatedCar){
        try {
            Car car = carRepository.findByIdForUpdate(updatedCar.getVIN())
                    .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", updatedCar.getVIN()));
            if (car.getStatus() == Car.Status.InUse && updatedCar.getStatus() == Car.Status.InUse)
                return Boolean.FALSE;
            car.setStatus(updatedCar.getStatus());
            return Boolean.TRUE;
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при изменении статуса автомобиля: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handleDelete(String vin){
        try {
            Car car = carRepository.findByIdForUpdate(vin)
                    .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", vin));
            if (car.getStatus() == Car.Status.Available)
                carRepository.deleteById(vin);
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при удалении автомобиля: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
