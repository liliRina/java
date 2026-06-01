package car.leasing.cars;

import car.leasing.MainHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Garage {
    private CarsDB carsDB;

    public Garage() {
        carsDB = new CarsDB();
    }

    public boolean addCar() {
        Scanner scanner = MainHandler.scanner;

        System.out.println("Введите VIN или 0 для возврата");
        String VIN = readVIN(scanner);
        if (VIN.equals(""))
            return false;

        System.out.println("Введите марку или 0 для возврата");
        String brand = scanner.nextLine().strip();
        if (brand.equals("0"))
            return false;

        System.out.println("Введите модель или 0 для возврата");
        String model = scanner.nextLine().strip();
        if (model.equals("0"))
            return false;

        System.out.println("Введите год выпуска или 0 для возврата");
        String year = readYear(scanner);
        if (year.equals(""))
            return false;

        System.out.println("Введите стоимость или 0 для возврата");
        BigDecimal price = readPrice(scanner);
        if(price.compareTo(BigDecimal.ZERO) == 0)
            return false;

        if (carsDB.getCarByVIN(VIN) != null){
            System.out.println("Машина с таким VIN уже существует");
            return false;
        }
        Car car;
        try {
            car = new Car(VIN, brand, model, year, price, null);
        } catch (CarCreateException e){
            System.out.println("Ошибка создания автомобиля: " + e);
            return false;
        }
        carsDB.saveNewCar(car);
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
        String brand = scanner.nextLine();
        if (brand.equals("0"))
            return;
        System.out.println("СПИСОК АВТОМОБИЛЕЙ марки " + brand +":");
        carsDB.getCarsByBrand(brand).stream()
                .forEach(System.out::println);
    }
    public void showCarsByModel() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите модель или 0 для возврата");
        String model = scanner.nextLine();
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
                price = scanner.nextBigDecimal();
                scanner.nextLine();
                price = price.setScale(6, RoundingMode.HALF_UP);
                if (price.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Стоимость меньше нуля");
                break;
            } catch (InputMismatchException | IllegalArgumentException e) {
                if (e.getClass() == InputMismatchException.class)
                    scanner.nextLine();
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
        if (VIN.equals(""))
            return;
        carsDB.deleteCarByVIN(VIN);
    }
}