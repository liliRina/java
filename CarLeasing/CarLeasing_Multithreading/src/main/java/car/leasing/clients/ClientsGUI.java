package car.leasing.clients;

import car.leasing.MainHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class ClientsGUI {
    private final ClientBase clientBase;
    public ClientsGUI(ClientBase clientBase){
        this.clientBase = clientBase;
    }
    public void clientMenu(){
        int action = 1;
        while (action !=  0) {
            System.out.println(
                    "=== КЛИЕНТЫ ===\n" +
                            "1. Добавить клиента\n" +
                            "2. Список клиентов\n" +
                            "3. Поиск по ФИО/паспорту\n" +
                            "0. Назад");
            action = MainHandler.readAction(4);
            if (!clientBase.isAvailable()) {
                System.out.println("Данные еще загружаются, пожалуйста, подождите...");
                continue;
            }
            switch (action) {
                case 1 -> clientBase.addClient();
                case 2 -> clientBase.showClients();
                case 3 -> findClientMenu(clientBase);
                case 4 -> testAddClient();
                default -> {}
            }
        }
    }

    private void testAddClient() {
        Random random = new Random();
        String input =
                        " dfgfhfghfh dfghfdgd \n" + // ФИО всё можно
                        random.nextInt(10000, 100000) + "12345\n" +
                        "+7" + random.nextInt(10000, 100000) + "12345\n";
        InputStream originalIn = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        MainHandler.scanner = new Scanner(System.in);
        clientBase.addClient();
        MainHandler.scanner = new Scanner(originalIn);
        MainHandler.scanner.useLocale(Locale.US);
        System.setIn(originalIn);
    }

    private void findClientMenu(ClientBase clientBase) {
        Integer action = 1;
        while(action != 0){
            System.out.println(
                    "=== ПОИСК КЛИЕНТОВ ===\n" +
                            "1. Поиск по ФИО\n" +
                            "2. Поиск по номеру паспорта\n" +
                            "0. Назад");
            action = MainHandler.readAction(2);
            if (!clientBase.isAvailable()) {
                System.out.println("Данные еще загружаются, пожалуйста, подождите...");
                continue;
            }
            switch (action){
                case 1 -> clientBase.showClientsByFullName();
                case 2 -> clientBase.showClientByPassportNumber();
                default -> {}
            }
        }
    }
}
