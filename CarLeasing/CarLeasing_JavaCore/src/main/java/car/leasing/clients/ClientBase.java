package car.leasing.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import car.leasing.MainHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientBase {
    private List<Client> clients = new ArrayList<>();
    private File file = new File("src/main/resources/clients.json");

    public ClientBase() {
        try {
            if (!file.exists()){
                file.createNewFile();
                new ObjectMapper().writeValue(file, new ArrayList<>());
            }
            else
                readClients();
        } catch (IOException e) {
            System.out.println("Не удалось открыть/создать файл с клиентами");
            throw new RuntimeException(e);
        }
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

        try {
            if (clients.stream().anyMatch(c -> c.getPassportNumber().equals(passportNumber))) {
                System.out.println("Клиент с таким паспортом существует");
                return false;
            }

            if (clients.stream().anyMatch(c -> c.getPhoneNumber().equals(phoneNumber))) {
                System.out.println("Клиент с таким номером телефона существует");
                return false;
            }

            Long newID = clients.size() != 0 ? clients.getLast().getID() + 1 : 1;
            Client client = new Client(newID, fullName, passportNumber, phoneNumber);
            clients.add(client);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, clients);
            System.out.println("Клиент успешно добавлен!");
            return true;
        } catch (ClientCreateException e) {
            System.out.println("Ошибка создания клиента: " + e);
        } catch (IOException e2) {
            System.out.println("Ошибка записи в файл: " + e2);
        }
        return false;
    }
    public void showClients() {
        System.out.println("СПИСОК КЛИЕНТОВ:");
        clients.stream().forEach(System.out::println);
    }

    public void showClientsByFullName() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите ФИО или 0 для возврата");
        String name = readFullName(scanner);
        if (name.equals(""))
            return;
        System.out.println("СПИСОК КЛИЕНТОВ с ФИО " + name + ":");
        clients.stream()
                .filter(n -> n.getFullName().equals(name))
                .forEach(System.out::println);
    }
    public void showClientByPassportNumber() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер паспорта или 0 для возврата");
        String passportNumber = readPassportNumber(scanner);
        if (passportNumber.equals(""))
            return;
        System.out.println("КЛИЕНТ с номером паспорта " + passportNumber + ":");
        clients.stream()
                .filter(n -> n.getPassportNumber().equals(passportNumber))
                .forEach(System.out::println);
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
    private boolean readClients() throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            clients = mapper.readValue(file, new TypeReference<>() {
            });
            return true;
        } catch (JsonMappingException e) {
            System.out.println("Некорректный файл с клиентами");
            throw e;
        }
    }

    public Client getClientByID() {
        Scanner scanner = MainHandler.scanner;
        Client client;
        while (true) {
            System.out.println("Введите ID клиента или 0 для возврата");
            try {
                Long ID = Long.valueOf(scanner.nextLine().strip());
                if (ID.equals(0L))
                    return null;
                client = clients.stream().filter(cl -> cl.getID().equals(ID)).findFirst().orElse(null);
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
            client = clients.stream().filter(cl -> cl.getPhoneNumber().equals(phoneNumber)).findFirst().orElse(null);
            if (client != null)
                return client;
            System.out.println("Клиент с номером телефона: " + phoneNumber + " не найден. Попробуйте ещё раз");
        }
    }
}
