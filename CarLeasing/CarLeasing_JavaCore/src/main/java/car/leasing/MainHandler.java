package car.leasing;

import car.leasing.cars.Car;
import car.leasing.cars.Garage;
import car.leasing.clients.Client;
import car.leasing.clients.ClientBase;
import car.leasing.contracts.Chancellery;
import car.leasing.contracts.LeasingContract;

import java.util.Locale;
import java.util.Scanner;

public class MainHandler {
    static public Scanner scanner = new Scanner(System.in);
    MainHandler(){
        scanner.useLocale(Locale.US);
    }
    public void mainMenu(){
        while(true){
            System.out.println(
                    "=== АВТОЛИЗИНГ ===\n" +
                    "Выберите действие:\n" +
                    "1. Управление автомобилями\n" +
                    "2. Управление клиентами\n" +
                    "3. Управление договорами\n" +
                    "4. Управление платежами\n" +
                    "0. Выход\n"
            );

            Integer action = readAction(4);
            switch (action){
                case 0 -> {return;}
                case 1 -> carMenu();
                case 2 -> clientMenu();
                case 3 -> contractMenu();
                case 4 -> paymentMenu();
                default -> {}
            }
        }
    }

    private void carMenu(){
        int action = 1;
        Garage garage = new Garage();
        while (action !=  0) {
            System.out.println(
                    "=== АВТОМОБИЛИ ===\n" +
                    "1. Добавить автомобиль\n" +
                    "2. Список всех автомобилей\n" +
                    "3. Поиск по критериям\n" +
                    "0. Назад"
            );
            action = readAction(3);
            switch (action) {
                case 1 -> garage.addCar();
                case 2 -> garage.showCars();
                case 3 -> findCarMenu(garage);
                default -> {}
            }
        }
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
            action = readAction(3);
            switch (action){
                case 1 -> garage.showCarsByBrand();
                case 2 -> garage.showCarsByModel();
                case 3 -> garage.showCarsByYear();
                default -> {}
            }
        }
    }

    private void clientMenu(){
        int action = 1;
        ClientBase clientBase = new ClientBase();
        while (action !=  0) {
            System.out.println(
                    "=== КЛИЕНТЫ ===\n" +
                    "1. Добавить клиента\n" +
                    "2. Список клиентов\n" +
                    "3. Поиск по ФИО/паспорту\n" +
                    "0. Назад");
            action = readAction(3);
            switch (action) {
                case 1 -> clientBase.addClient();
                case 2 -> clientBase.showClients();
                case 3 -> findClientMenu(clientBase);
                default -> {}
            }
        }
    }
    private void findClientMenu(ClientBase clientBase) {
        Integer action = 1;
        while(action != 0){
            System.out.println(
                    "=== ПОИСК КЛИЕНТОВ ===\n" +
                    "1. Поиск по ФИО\n" +
                    "2. Поиск по номеру паспорта\n" +
                    "0. Назад");
            action = readAction(2);
            switch (action){
                case 1 -> clientBase.showClientsByFullName();
                case 2 -> clientBase.showClientByPassportNumber();
                default -> {}
            }
        }
    }

    private void contractMenu() {
        int action = 1;
        Garage garage = new Garage();
        ClientBase clientBase = new ClientBase();
        Chancellery chancellery = new Chancellery();
        while (action !=  0) {
            System.out.println(
                    "=== ДОГОВОРЫ ===\n" +
                    "1. Создать договор\n" +
                    "2. Найти договор по номеру\n" +
                    "3. Активные договоры клиента\n" +
                    "4. История по клиенту\n" +
                    "0. Назад"
            );
            action = readAction(4);
            switch (action) {
                case 1 -> createContract(garage, clientBase, chancellery);
                case 2 -> chancellery.showContractByID();
                case 3 -> chancellery.showActiveContractsByClient(clientBase);
                case 4 -> chancellery.showContractsByClient(clientBase);
                default -> {}
            }
        }
    }
    private int chooseSearchingClientParam(){
        System.out.println(
                "=== ВЫБОР КЛИЕНТА ===\n" +
                "1. Выбор по ID\n" +
                "2. Выбор по номеру телефона\n" +
                "0. Назад"
        );
        int action = readAction(2);
        return action;
    }
    private void createContract(Garage garage, ClientBase clientBase, Chancellery chancellery) {
        int ActionCode = chooseSearchingClientParam();
        Client client;
        switch (ActionCode){
            case 1 -> client = clientBase.getClientByID();
            case 2 -> client = clientBase.getClientByPhone();
            default -> {return;}
        }
        if (client == null)
            return;

        Car car = garage.getCarByVin();
        if (car == null)
            return;
        if (chancellery.addContract(car, client))
            garage.rewriteCars(car);
    }

    private void paymentMenu() {
        int action = 1;
        Chancellery chancellery = new Chancellery();
        Garage garage = new Garage();
        while (action !=  0) {
            System.out.println(
                    "=== ПЛАТЕЖИ ===\n" +
                    "1. Зарегистрировать платеж\n" +
                    "0. Назад"
            );
            action = readAction(1);
            switch (action) {
                case 1:
                    LeasingContract contract = chancellery.getContractByID();
                    if (contract == null)
                        break;

                    if(chancellery.pay(contract))
                        if (contract.isClosed())
                            garage.setAvailableStatus(contract.getCar());
                    break;
                default:{}
            }
        }
    }


    private Integer readAction(Integer ceiling) {
        Integer action;
        while(true){
            try {
                action = Integer.parseInt(scanner.nextLine().strip());
                if (action < 0 || action > ceiling)
                    throw new RuntimeException();
                break;
            } catch (java.util.NoSuchElementException e){
                throw e;
            } catch (RuntimeException e){
                System.out.println("Введите корретный код действия");
            }
        }
        return action;
    }
}
