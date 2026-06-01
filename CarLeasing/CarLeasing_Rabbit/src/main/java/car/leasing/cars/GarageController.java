package car.leasing.cars;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import car.leasing.cars.domain.Car;
import car.leasing.cars.domain.CarDTO;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class GarageController {
    private final Garage garage;
    public GarageController(Garage garage){
        this.garage = garage;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createCar(@Valid @RequestBody CarDTO carDTO) {
        garage.addCar(carDTO);
        return ResponseEntity
                .accepted()
                .body("Машина отправлена на обработку");
    }

    @GetMapping("")
    public List<Car> getCars(){
        return garage.getCars();
    }

    @GetMapping("/search/vin/{vin}")
    public Car getCarByVin(@PathVariable String vin){
        return garage.getCarByVin(vin.strip());
    }
    @GetMapping("/search/brand/{brand}")
    public List<Car> getCarByBrand(@PathVariable String brand){
        return garage.getCarsByBrand(brand.strip());
    }
    @GetMapping("/search/model/{model}")
    public List<Car> getCarByModel(@PathVariable String model){
        return garage.getCarsByModel(model.strip());
    }
    @GetMapping("/search/year/{year}")
    public List<Car> getCarByYear(@PathVariable Integer year){
        return garage.getCarsByYear(year);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("vin/{vin}")
    public ResponseEntity<String> deleteCar(@PathVariable String vin) {
        garage.deleteCar(vin.strip());
        return ResponseEntity
                .accepted()
                .body("Машина отправлена на обработку");
    }
}
