package car.leasing.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import car.leasing.MainHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Garage {
    private final CarsDB carsDB;
    private static final Logger log = LoggerFactory.getLogger(Garage.class);
    public Garage() {
        carsDB = new CarsDB();
    }
    public Garage(CarsDB carsDB){
        this.carsDB = carsDB;
    }

    public boolean addCar() {
        Scanner scanner = MainHandler.scanner;

        System.out.println("Введите VIN или 0 для возврата");
        String VIN = readVIN(scanner);
        if (VIN.isEmpty())
            return false;

        System.out.println("Введите марку или 0 для возврата");
        String brand = scanner.nextLine().strip();
        log.info("Введена марка: {}", brand);
        if (brand.equals("0"))
            return false;

        System.out.println("Введите модель или 0 для возврата");
        String model = scanner.nextLine().strip();
        log.info("Введена модель: {}", model);
        if (model.equals("0"))
            return false;

        System.out.println("Введите год выпуска или 0 для возврата");
        String year = readYear(scanner);
        if (year.isEmpty())
            return false;

        System.out.println("Введите стоимость или 0 для возврата");
        BigDecimal price = readPrice(scanner);
        if(price.compareTo(BigDecimal.ZERO) == 0)
            return false;

        if (carsDB.getCarByVIN(VIN) != null){
            System.out.println("Автомобиль с таким VIN уже существует");
            return false;
        }
        Car car;
        try {
            car = new Car(VIN, brand, model, year, price, null);
        } catch (CarCreateException e){
            System.out.println("Ошибка при создании автомобиля");
            return false;
        }
        if (carsDB.saveNewCar(car))
            System.out.println("Автомобиль успешно добавлен!");
        else
            System.out.println("Не удалось добавить автомобиль");
        return true;
    }

    public void showCars(){
        System.out.println("СПИСОК АВТОМОБИЛЕЙ:");
        carsDB.getCars().stream()
                .forEach(System.out::println);
    }

    public void showCarsByBrand(){
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите марку или 0 для возврата");
        String brand = scanner.nextLine().strip();
        log.info("Введена марка: {}", brand);
        if (brand.equals("0"))
            return;
        System.out.println("СПИСОК АВТОМОБИЛЕЙ марки " + brand +":");
        carsDB.getCarsByBrand(brand).stream()
                .forEach(System.out::println);
    }
    public void showCarsByModel() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите модель или 0 для возврата");
        String model = scanner.nextLine().strip();
        log.info("Введена модель: {}", model);
        if (model.equals("0"))
            return;
        System.out.println("СПИСОК АВТОМОБИЛЕЙ модели " + model +":");
        carsDB.getCarsByModel(model).stream()
                .forEach(System.out::println);
    }
    public void showCarsByYear() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите год выпуска или 0 для возврата");
        String year = readYear(scanner);
        if (year.equals(""))
            return;
        System.out.println("СПИСОК АВТОМОБИЛЕЙ года " + year +":");
        carsDB.getCarsByYear(year).stream()
                .forEach(System.out::println);
    }

    public Car getCarByVin(){
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите VIN или 0 для возврата");
        Car car;
        while (true) {
            String VIN = readVIN(scanner);
            if (VIN.equals(""))
                return null;

            car = carsDB.getCarByVIN(VIN);
            if (car != null){
                if (car.getStatus() == Car.Status.InUse)
                    System.out.println("Автомобиль с VIN: " + VIN + " занят. Попробуйте ещё раз");
                else
                    return car;
            }
            else
                System.out.println("Автомобиль с VIN: " + VIN + " не найден. Попробуйте ещё раз");
        }
    }
    public String readYear(Scanner scanner){
        String year;
        while(true) {
            year = scanner.nextLine().strip();
            log.info("Введён год: {}", year);
            if(year.equals("0"))
                return "";
            if (!Car.checkYear(year))
                System.out.println("Год должен быть в диапазоне от 2000 до 2026");
            else
                break;
        }
        return year;
    }
    private String readVIN(Scanner scanner){
        String VIN;
        while(true) {
            VIN = scanner.nextLine().strip();
            log.info("Введён VIN: {}", VIN);
            if (VIN.equals("0"))
                return "";
            if (!Car.checkVIN(VIN))
                System.out.println("VIN должен состоять из 17 знаков: цифры и латинские буквы");
            else
                break;
        }
        return VIN;
    }
    private BigDecimal readPrice(Scanner scanner){
        BigDecimal price;
        while (true){
            try {
                String input = scanner.nextLine().strip();
                log.info("Введена стоимость: {}", input);
                price = new BigDecimal(input);
                price = price.setScale(6, RoundingMode.HALF_UP);
                if (price.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Стоимость меньше нуля");
                break;
            } catch (InputMismatchException | IllegalArgumentException e) {
                System.out.println("Стоимость должна быть положительным числом");
            }
        }
        return price;
    }

    public boolean setStatus(Car car, Car.Status status) {
        return carsDB.setStatusCars(car, status);
    }

    public void deleteCar() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите VIN или 0 для возврата");
        String VIN = readVIN(scanner);
        if (VIN.isEmpty())
            return;
        if (carsDB.deleteCarByVIN(VIN))
            System.out.println("Автомобиль с VIN " + VIN + " успешно удалена!");
        else
            System.out.println("Не удалось удалить автомобиль с VIN " + VIN + ": не найдена или есть активный договор");
    }
}