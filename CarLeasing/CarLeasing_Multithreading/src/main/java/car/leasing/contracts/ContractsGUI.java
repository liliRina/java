package car.leasing.contracts;

import car.leasing.MainHandler;
import car.leasing.cars.Car;
import car.leasing.cars.Garage;
import car.leasing.clients.Client;
import car.leasing.clients.ClientBase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

public class ContractsGUI {
    Garage garage;
    ClientBase clientBase;
    Chancellery chancellery;
    public ContractsGUI(Garage garage, ClientBase clientBase, Chancellery chancellery){
        this.garage = garage;
        this.clientBase = clientBase;
        this.chancellery = chancellery;
    }
    public void contractMenu() {
        int action = 1;
        while (action !=  0) {
            System.out.println(
                    "=== ДОГОВОРЫ ===\n" +
                            "1. Создать договор\n" +
                            "2. Найти договор по номеру\n" +
                            "3. Активные договоры клиента\n" +
                            "4. История по клиенту\n" +
                            "0. Назад"
            );
            action = MainHandler.readAction(5);
            if (!chancellery.isAvailable()) {
                System.out.println("Данные еще загружаются, пожалуйста, подождите...");
                continue;
            }
            switch (action) {
                case 1 -> {
                    if (!garage.isAvailable() || !clientBase.isAvailable() || !chancellery.isAvailable()) {
                        String objs = "";
                        if (!garage.isAvailable())
                            objs += " Машины";
                        if(!clientBase.isAvailable())
                            objs += " Клиенты";
                        if(!chancellery.isAvailable())
                            objs += " Договоры";
                        System.out.println("Данные" + " (" + objs + ") " + "еще загружаются, пожалуйста, подождите...");
                        continue;
                    }
                    createContract(garage, clientBase, chancellery);
                }
                case 2 -> chancellery.showContractByID();
                case 3 -> chancellery.showActiveContractsByClient(clientBase);
                case 4 -> chancellery.showContractsByClient(clientBase);
                case 5 -> {
                    if (!garage.isAvailable() || !clientBase.isAvailable() || !chancellery.isAvailable()) {
                        String objs = "";
                        if (!garage.isAvailable())
                            objs += " Машины";
                        if(!clientBase.isAvailable())
                            objs += " Клиенты";
                        if(!chancellery.isAvailable())
                            objs += " Договоры";
                        System.out.println("Данные" + " (" + objs + ") " + "еще загружаются, пожалуйста, подождите...");
                        continue;
                    }
                    testAddContract();
                }
                default -> {}
            }
        }
    }
    private void testAddContract(){
        String input =
                        "1\n" +  // выбор ID клиента
                        "1\n" +
                        "5NPD84LF2KH123456\n" +
                        "2\n" +
                        "69.77777\n" +
                        "45.6\n";
        InputStream originalIn = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        MainHandler.scanner = new Scanner(System.in);
        MainHandler.scanner.useLocale(Locale.US);
        createContract(garage, clientBase, chancellery);
        MainHandler.scanner = new Scanner(originalIn);
        MainHandler.scanner.useLocale(Locale.US);
        System.setIn(originalIn);
    }
    private int chooseSearchingClientParam(){
        System.out.println(
                "=== ВЫБОР КЛИЕНТА ===\n" +
                        "1. Выбор по ID\n" +
                        "2. Выбор по номеру телефона\n" +
                        "0. Назад"
        );
        int action = MainHandler.readAction(2);
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
            garage.setStatus(car, Car.Status.InUse);
    }
}
