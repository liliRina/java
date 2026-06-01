package car.leasing;

import car.leasing.cars.Car;
import car.leasing.cars.CarsGUI;
import car.leasing.cars.Garage;
import car.leasing.clients.Clients;
import car.leasing.clients.ClientsGUI;
import car.leasing.contracts.Chancellery;
import car.leasing.contracts.ContractsGUI;
import car.leasing.contracts.LeasingContract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import java.util.concurrent.*;

public class MainHandler {
    static public Scanner scanner = new Scanner(System.in);
    private final Garage garage;
    private final Clients clients;
    private final Chancellery chancellery;
    private final CarsGUI carsGUI;
    private final ClientsGUI clientsGUI;
    private final ContractsGUI contractsGUI;

    MainHandler(){
        scanner.useLocale(Locale.US);
        garage = new Garage();
        clients = new Clients();
        chancellery = new Chancellery();

        clientsGUI = new ClientsGUI(clients);
        contractsGUI = new ContractsGUI(garage, clients, chancellery);
        carsGUI = new CarsGUI(garage);
        Main.sleep(1000);
    }
    MainHandler(Scanner scanner){
        this();
        this.scanner = scanner;
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
                case 0 -> {
                    System.out.println("Работа с лизингом завершается");
                    ExecutorService closingPool = Executors.newFixedThreadPool(3);
                    try {
                        closingPool.invokeAll(List.of(
                              (Callable<Void>) () -> {chancellery.close(); return null;}));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    closingPool.shutdown();
                    return;
                }
                case 1 -> carsGUI.carMenu();
                case 2 -> clientsGUI.clientMenu();
                case 3 -> contractsGUI.contractMenu();
                case 4 -> paymentMenu();
                default -> {}
            }
        }
    }

    private void paymentMenu() {
        int action = 1;
        while (action != 0) {
            System.out.println(
                    "=== ПЛАТЕЖИ ===\n" +
                            "1. Зарегистрировать платеж\n" +
                            "0. Назад"
            );
            action = readAction(1);
            if (!chancellery.isAvailableForPayments()) {
                System.out.println("Данные еще загружаются, пожалуйста, подождите...");
                continue;
            }
            switch (action) {
                case 1:
                    LeasingContract contract = chancellery.getContractByID();
                    if (contract == null)
                        break;
                    BigDecimal receivedPayment = chancellery.getUserPayment(contract);
                    if (receivedPayment.compareTo(BigDecimal.ZERO) == 0)
                        break;

                    new Thread(() -> {
                        synchronized (contract) {
                            BigDecimal curPayment = chancellery.getCurrentPayment(contract).getPayment();
                            if (curPayment == null) {
                                chancellery.finishContractProcess(contract);
                                return;
                            }
                            if (curPayment.compareTo(receivedPayment) != 0){
                                System.out.println("Сумма не соответствует требуемому платежу");
                                chancellery.finishContractProcess(contract);
                                return;
                            }
                            if (!chancellery.pay(contract)){
                                chancellery.finishContractProcess(contract);
                                return;
                            }

                            if (!chancellery.hasActiveContractsByClient(contract.getClient()))
                                clients.setClientStatus(contract.getClient(), false);
                            garage.setStatus(contract.getCar(), Car.Status.Available);
                            chancellery.finishContractProcess(contract);
                        }
                    }).start();
                    break;
                default: {
                }
            }
        }
    }

    public static Integer readAction(Integer ceiling) {
        Integer action;
        while(true){
            try {
                action = Integer.parseInt(scanner.nextLine().strip());
                if (action < 0 || action > ceiling)
                    throw new RuntimeException();
                break;
            } catch (java.util.NoSuchElementException e){
                Main.sleep(1000);
                throw e;
            } catch (RuntimeException e){
                System.out.println("Введите корретный код действия");
            }
        }
        return action;
    }
}
