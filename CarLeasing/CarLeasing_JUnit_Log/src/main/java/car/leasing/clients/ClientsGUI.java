package car.leasing.clients;

import car.leasing.MainHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class ClientsGUI {
    private final Clients clients;
    public ClientsGUI(Clients clients){
        this.clients = clients;
    }
    public void clientMenu(){
        int action = 1;
        while (action !=  0) {
            System.out.println(
                    "=== КЛИЕНТЫ ===\n" +
                            "1. Добавить клиента\n" +
                            "2. Список клиентов\n" +
                            "3. Поиск по ФИО/паспорту\n" +
                            "4. Удалить клиента\n" +
                            "0. Назад");
            action = MainHandler.readAction(4);
            switch (action) {
                case 1 -> clients.addClient();
                case 2 -> clients.showClients();
                case 3 -> findClientMenu(clients);
                case 4 -> clients.deleteClient();
                case 5 -> testAddClient();
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
        clients.addClient();
        MainHandler.scanner = new Scanner(originalIn);
        MainHandler.scanner.useLocale(Locale.US);
        System.setIn(originalIn);
    }

    private void findClientMenu(Clients clients) {
        Integer action = 1;
        while(action != 0){
            System.out.println(
                    "=== ПОИСК КЛИЕНТОВ ===\n" +
                            "1. Поиск по ФИО\n" +
                            "2. Поиск по номеру паспорта\n" +
                            "0. Назад");
            action = MainHandler.readAction(2);
            switch (action){
                case 1 -> clients.showClientsByFullName();
                case 2 -> clients.showClientByPassportNumber();
                default -> {}
            }
        }
    }
}
