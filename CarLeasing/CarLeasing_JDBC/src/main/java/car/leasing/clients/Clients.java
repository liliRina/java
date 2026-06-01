package car.leasing.clients;

import car.leasing.MainHandler;

import java.util.Scanner;

public class Clients {
    private final ClientsDB clientsDB;

    public Clients() {
        clientsDB = new ClientsDB();
    }
    public boolean addClient() {
        Scanner scanner = MainHandler.scanner;

        System.out.println("Введите ФИО или 0 для возврата");
        String fullName = readFullName(scanner);
        if (fullName.equals(""))
            return false;

        System.out.println("Введите номер паспорта или 0 для возврата");
        String passportNumber = readPassportNumber(scanner);
        if (passportNumber.equals(""))
            return false;

        System.out.println("Введите телефонный номер в формате: знак '+7' и 10 цифр или 0 для возврата");
        String phoneNumber = readPhoneNumber(scanner);
        if (phoneNumber.equals(""))
            return false;

        if (clientsDB.getClientByPassportNumber(passportNumber) != null) {
            System.out.println("Клиент с таким паспортом существует");
            return false;
        }
        if (clientsDB.getClientByPhoneNumber(phoneNumber) != null) {
            System.out.println("Клиент с таким номером телефона существует");
            return false;
        }
        Client client;
        try {
            client = new Client(null, fullName, passportNumber, phoneNumber);
        } catch (ClientCreateException e) {
            System.out.println("Ошибка создания клиента: " + e);
            return false;
        }
        clientsDB.saveNewClient(client);
        return true;
    }

    public void showClients() {
        System.out.println("СПИСОК КЛИЕНТОВ:");
        clientsDB.getClients()
                .forEach(System.out::println);
    }
    public void showClientsByFullName() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите ФИО или 0 для возврата");
        String name = readFullName(scanner);
        if (name.isEmpty())
            return;
        System.out.println("СПИСОК КЛИЕНТОВ с ФИО " + name + ":");
        clientsDB.getClientsByFullName(name)
                .forEach(System.out::println);
    }
    public void showClientByPassportNumber() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер паспорта или 0 для возврата");
        String passportNumber = readPassportNumber(scanner);
        if (passportNumber.isEmpty())
            return;
        System.out.println("КЛИЕНТ с номером паспорта " + passportNumber + ":");
        Client client = clientsDB.getClientByPassportNumber(passportNumber);
        System.out.println(client == null ? "" : client);
    }

    public boolean setClientStatus(Client client, boolean doWithContract){
        return clientsDB.setClientStatus(client, doWithContract);
    }

    public String readFullName(Scanner scanner) {
        String name;
        while (true) {
            name = scanner.nextLine().strip();
            if (name.equals("0"))
                return "";
            if (!Client.checkFullName(name))
                System.out.println("Имя должно содержать буквы и разделители пробел и -");
            else
                break;
        }
        return name;
    }
    private String readPassportNumber(Scanner scanner) {
        String passportNumber;
        while (true) {
            passportNumber = scanner.nextLine().strip();
            if (passportNumber.equals("0"))
                return "";
            if (!Client.checkPassportNumber(passportNumber))
                System.out.println("Номер паспорта должен состоять из 10 цифр");
            else
                break;
        }
        return passportNumber;
    }
    public String readPhoneNumber(Scanner scanner) {
        String phoneNumber;
        while (true) {
            phoneNumber = scanner.nextLine().strip();
            if (phoneNumber.equals("0"))
                return "";
            if (!Client.checkPhoneNumber(phoneNumber))
                System.out.println("Номер телефона должен состоять +7 и 10 цифр");
            else
                break;
        }
        return phoneNumber;
    }

    public Client getClientByID() {
        Scanner scanner = MainHandler.scanner;
        Client client;
        while (true) {
            System.out.println("Введите ID клиента или 0 для возврата");
            try {
                Integer ID = Integer.valueOf(scanner.nextLine().strip());
                if (ID.equals(0))
                    return null;
                client = clientsDB.getClientById(ID);
                if (client != null)
                    return client;
                System.out.println("Клиент с ID: " + ID + " не найден. Попробуйте ещё раз");
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число");
            }
        }
    }
    public Client getClientByPhone() {
        Scanner scanner = MainHandler.scanner;
        Client client;
        while (true) {
            System.out.println("Введите номер телефона клиента или 0 для возврата");
            String phoneNumber = readPhoneNumber(scanner);
            if (phoneNumber.equals(""))
                return null;
            client = clientsDB.getClientByPhoneNumber(phoneNumber);
            if (client != null)
                return client;
            System.out.println("Клиент с номером телефона: " + phoneNumber + " не найден. Попробуйте ещё раз");
        }
    }

    public void deleteClient() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер паспорта или 0 для возврата");
        String passportNumber = readPassportNumber(scanner);
        if (passportNumber.equals(""))
            return;
        clientsDB.deleteClient(passportNumber);
    }
}
