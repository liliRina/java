package car.leasing.contracts;

import car.leasing.MainHandler;
import car.leasing.cars.Car;
import car.leasing.cars.Garage;
import car.leasing.clients.Client;
import car.leasing.clients.Clients;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

public class ContractsGUI {
    Garage garage;
    Clients clients;
    Chancellery chancellery;
    public ContractsGUI(Garage garage, Clients clients, Chancellery chancellery){
        this.garage = garage;
        this.clients = clients;
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
                case 1 -> createContract(garage, clients, chancellery);
                case 2 -> chancellery.showContractByID();
                case 3 -> chancellery.showActiveContractsByClient(clients);
                case 4 -> chancellery.showContractsByClient(clients);
                case 5 -> {
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
        createContract(garage, clients, chancellery);
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
    private void createContract(Garage garage, Clients clients, Chancellery chancellery) {
        int ActionCode = chooseSearchingClientParam();
        Client client;
        switch (ActionCode){
            case 1 -> client = clients.getClientByID();
            case 2 -> client = clients.getClientByPhone();
            default -> {return;}
        }
        if (client == null)
            return;
        Car car = garage.getCarByVin();
        if (car == null)
            return;
        Runnable onContractErrorOrCancel = () -> {
            garage.setStatus(car, Car.Status.Available);
            if(!chancellery.hasActiveContractByClientPassport(client))
                clients.setClientStatus(client, false);
        };

        if (garage.setStatus(car, Car.Status.InUse)){
            if (clients.setClientStatus(client, true)){
                if (!chancellery.processingAddContract(car, client, onContractErrorOrCancel)){
                    onContractErrorOrCancel.run();
                }
            }
            else{
                garage.setStatus(car, Car.Status.Available);
            }
        }
    }
}
