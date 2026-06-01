package car.leasing.cars;

import car.leasing.MainHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class CarsGUI {
    private final Garage garage;
    public CarsGUI(Garage garage){
        this.garage = garage;
    }
    public void carMenu(){
        int action = 1;
        while (action !=  0) {
            System.out.println(
                    "=== АВТОМОБИЛИ ===\n" +
                            "1. Добавить автомобиль\n" +
                            "2. Список всех автомобилей\n" +
                            "3. Поиск по критериям\n" +
                            "0. Назад"
            );
            action = MainHandler.readAction(4);
            if (!garage.isAvailable()) {
                System.out.println("Данные еще загружаются, пожалуйста, подождите...");
                continue;
            }
            switch (action) {
                case 1 -> garage.addCar();
                case 2 -> garage.showCars();
                case 3 -> findCarMenu(garage);
                case 4 -> testAddCar(garage);
                default -> {}
            }
        }
    }
    private void testAddCar(Garage garage){
        Random random = new Random();
        String input =
                + random.nextInt(1000, 10000) + "qweRTYuioQWEr\n" +
                        " 123456789qwerYUIu \n" + // корректный VIN
                        " BMVХхъ{}{???///++...!#$%^&*( \n" + // марка всё можно!
                        " BMVх5Ххъ{}{???///++...!#$%^&*( \n" + // модель всё можно!
                        "  2007 \n" + //корректный год
                        "23.9\n";
        InputStream originalIn = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        MainHandler.scanner = new Scanner(System.in);
        MainHandler.scanner.useLocale(Locale.US);
        garage.addCar();
        MainHandler.scanner = new Scanner(originalIn);
        MainHandler.scanner.useLocale(Locale.US);
        System.setIn(originalIn);

    }
    private void findCarMenu(Garage garage){
        Integer action = 1;
        while(action != 0){
            System.out.println(
                    "=== ПОИСК АВТОМОБИЛЕЙ ===\n" +
                            "1. Поиск по марке\n" +
                            "2. Поиск по модели\n" +
                            "3. Поиск по году выпуска\n" +
                            "0. Назад");
            action = MainHandler.readAction(3);
            if (!garage.isAvailable()) {
                System.out.println("Данные еще загружаются, пожалуйста, подождите...");
                continue;
            }
            switch (action){
                case 1 -> garage.showCarsByBrand();
                case 2 -> garage.showCarsByModel();
                case 3 -> garage.showCarsByYear();
                default -> {}
            }
        }
    }
}
