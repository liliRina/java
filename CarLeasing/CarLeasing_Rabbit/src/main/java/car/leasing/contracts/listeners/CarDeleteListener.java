package car.leasing.contracts.listeners;

import jakarta.persistence.PostRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import car.leasing.cars.domain.Car;
import car.leasing.contracts.Chancellery;

@Component
public class CarDeleteListener {
    private static Chancellery chancellery;

    @Autowired
    public void setChancellery(Chancellery chancellery) {
        CarDeleteListener.chancellery = chancellery;
    }

    @PostRemove
    public void onCarDelete(Car deletedCar) {
        chancellery.rewriteContracts();
        System.out.println("JSON файл обновлен после удаления машины с VIN: " + deletedCar.getVIN());
    }
}
