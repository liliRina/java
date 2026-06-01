package car.leasing.cars;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import car.leasing.MainHandler;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Garage {
    private List<Car> cars = new ArrayList<>();
    private File file = new File("src/main/resources/cars.json");

    public Garage() {
        try {
            if (!file.exists()){
                file.createNewFile();
                new ObjectMapper().writeValue(file, new ArrayList<>());
            }
            else
                readCars();
        } catch(IOException e){
            System.out.println("Не удалось открыть/создать файл с машинами");
            throw new RuntimeException(e);
        }
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

        try {
            if (cars.stream().anyMatch(c -> c.getVIN().equals(VIN))){
                System.out.println("Машина с таким VIN уже существует");
                return false;
            }
            Car car = new Car(VIN, brand, model, year, price, null);
            cars.add(car);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, cars);
            System.out.println("Автомобиль успешно добавлен!");
            return true;
        } catch (CarCreateException e){
            System.out.println("Ошибка создания автомобиля: " + e);
        } catch(IOException e2){
            System.out.println("Ошибка записи в файл: " + e2);
        }
        return false;
    }

    private boolean readCars() throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            cars = mapper.readValue(file, new TypeReference<List<Car>>() {});
            return true;
        } catch (JsonMappingException e) {
            System.out.println("Некорректный файл с машинами");
            throw e;
        }
    }

    public void showCars(){
        System.out.println("СПИСОК АВТОМОБИЛЕЙ:");
        cars.forEach(System.out::println);
    }

    public void showCarsByBrand(){
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите марку или 0 для возврата");
        String brand = scanner.nextLine();
        if (brand.equals("0"))
            return;
        System.out.println("СПИСОК АВТОМОБИЛЕЙ марки " + brand +":");
        cars.stream()
                .filter(car -> car.getBrand().equals(brand))
                .forEach(System.out::println);
    }

    public void showCarsByModel() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите модель или 0 для возврата");
        String model = scanner.nextLine();
        if (model.equals("0"))
            return;
        System.out.println("СПИСОК АВТОМОБИЛЕЙ модели " + model +":");
        cars.stream()
                .filter(car -> car.getModel().equals(model))
                .forEach(System.out::println);
    }

    public void showCarsByYear() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите год выпуска или 0 для возврата");
        String year = readYear(scanner);
        if (year.equals(""))
            return;
        System.out.println("СПИСОК АВТОМОБИЛЕЙ года " + year +":");
        cars.stream()
                .filter(car -> car.getYear().equals(year))
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
            car = cars.stream().filter(c -> c.getVIN().equals(VIN)).findFirst().orElse(null);
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

    public void setInUseStatus(Car car) {
        car.setStatus(Car.Status.InUse);
        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, cars);
        } catch(IOException e){
            System.out.println("Ошибка записи в файл с машинами: " + e);
        }
    }

    public void setAvailableStatus(Car car) {
        Car carInGarage = cars.stream().filter(c -> car.getVIN().equals(c.getVIN()))
                .findFirst().get();
        carInGarage.setStatus(Car.Status.Available);
        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, cars);
        } catch(IOException e){
            System.out.println("Ошибка записи в файл с машинами: " + e);
        }
    }

    public void rewriteCars(Car car) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, cars);
        } catch(IOException e){
            System.out.println("Ошибка записи в файл с машинами: " + e);
        }
    }
}