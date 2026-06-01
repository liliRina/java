package car.leasing.contracts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import car.leasing.MainHandler;
import car.leasing.cars.Car;
import car.leasing.clients.Client;
import car.leasing.clients.ClientBase;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Chancellery {
    private List<LeasingContract> contracts = new ArrayList<>();
    private File file = new File("src/main/resources/contracts.json");

    public Chancellery() {
        try {
            if (!file.exists()){
                file.createNewFile();
                new ObjectMapper().writeValue(file, new ArrayList<>());
            }
            else
                readContracts();
        } catch(IOException e){
            System.out.println("Не удалось открыть/создать файл с договорами");
            throw new RuntimeException(e);
        }
    }

    public boolean addContract(Car car, Client client) {
        Scanner scanner = MainHandler.scanner;

        System.out.println("Введите количество месяцев лизинга или 0 для возврата");
        Integer cntMonths = readCntMonth(scanner);
        if (cntMonths == 0)
            return false;


        System.out.println("Введите первоначальный взнос или 0 для возврата");
        BigDecimal initialPayment = readInitialPayment(scanner, car.getPrice());
        if (initialPayment.compareTo(BigDecimal.ZERO) == 0)
            return false;

        System.out.println("Введите процентную ставку или 0 для возврата");
        Double rate = readRate(scanner);

        if (rate < 0.00001)
            return false;

        try {
            Long newID = contracts.size() != 0 ? contracts.getLast().getContractNumber() + 1 : 1;
            LeasingContract contract = new LeasingContract(newID, car, client,
                    cntMonths, initialPayment, rate);
            car.setStatus(Car.Status.InUse);
            contracts.add(contract);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(file, contracts);
            System.out.println("Договор успешно добавлен!");
            return true;
        } catch (LeasingContractCreateException e){
            System.out.println("Ошибка создания договора: " + e);
        } catch(IOException e2){
            System.out.println("Ошибка записи в файл: " + e2);
        }
        return false;
    }

    public void showContractByID() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер договора или 0 для возврата");
        while (true){
            Long contractNumber;
            try {
                contractNumber = Long.valueOf(scanner.nextLine().strip());
                if (contractNumber.equals(0))
                    return;
                System.out.println("ДОГОВОР с номером " + contractNumber +":");
                contracts.stream()
                        .filter(con -> con.getContractNumber().equals(contractNumber))
                        .forEach(System.out::println);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число");
            }
        }
    }
    public void showContractsByClient(ClientBase clientBase) {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер телефона клиента или 0 для возврата");
        String phoneNumber = clientBase.readPhoneNumber(scanner);
        if (phoneNumber.equals(""))
            return;
        System.out.println("ИСТОРИЯ ДОГОВОРОВ клиента с номером телефона " + phoneNumber +":");
        contracts.stream()
                .filter(con -> con.getClient().getPhoneNumber().equals(phoneNumber))
                .forEach(System.out::println);

    }
    public void showActiveContractsByClient(ClientBase clientBase) {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер телефона клиента или 0 для возврата");
        String phoneNumber = clientBase.readPhoneNumber(scanner);
        if (phoneNumber.equals(""))
            return;
        System.out.println("АКТИВНЫЕ ДОГОВОРЫ клиента с номером телефона " + phoneNumber +":");
        contracts.stream()
                .filter(con -> con.getClient().getPhoneNumber().equals(phoneNumber) &&
                        con.getStatus() == LeasingContract.Status.ACTIVE)
                .forEach(System.out::println);
    }

    private BigDecimal readInitialPayment(Scanner scanner, BigDecimal carPrice){
        BigDecimal initialPayment;
        while (true){
            try {
                initialPayment = scanner.nextBigDecimal();
                scanner.nextLine();
                initialPayment = initialPayment.setScale(6, RoundingMode.HALF_UP);
                if(initialPayment.compareTo(carPrice) >= 0)
                    throw new IllegalArgumentException("Первоначальный взнос больше стоимости машины");
                break;
            } catch (InputMismatchException | IllegalArgumentException e) {
                if (e.getClass() == InputMismatchException.class)
                    scanner.nextLine();
                System.out.println("Введите положительное число меньше стоимости машины: " + carPrice);
            }
        }
        return initialPayment;
    }
    private Integer readCntMonth(Scanner scanner){
        Integer cntMonths;
        while(true) {
            try {
                cntMonths = Integer.valueOf(scanner.nextLine().strip());
                if (cntMonths == 0)
                    return 0;
                if(!LeasingContract.checkCntMonth(cntMonths))
                    throw new IllegalArgumentException("Количество месяцев или равно 0");
                break;
            } catch (NumberFormatException e) {
                System.out.println("Введите положительное целое число");
            }
        }
        return cntMonths;
    }
    private Double readRate(Scanner scanner){
        Double rate;
        while (true){
            try {
                rate = Double.valueOf(scanner.nextLine());
                if (rate < 0.000001)
                    return 0d;
                if (!LeasingContract.checkRate(rate))
                    throw new IllegalArgumentException("Процентная ставка больше/равна 100 или меньше/равно 0");
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Введите процентную ставку в виде числа от 0 до 100");
            }
        }
        return rate;
    }
    private boolean readContracts() throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            contracts = mapper.readValue(file, new TypeReference<>() {});
            return true;
        } catch (JsonMappingException e) {
            System.out.println("Некорректный файл с договорами");
            throw e;
        }
    }

    public boolean pay(LeasingContract contract) {
        if (contract.getStatus() == LeasingContract.Status.CLOSED){
            System.out.println("Договор уже закрыт");
            return false;
        }
        contract.showCurPayment();
        System.out.println("Введите сумму или 0 для возврата");
        BigDecimal cost = readPayment(contract.getCurrentPayment().getPayment());
        if (cost.compareTo(BigDecimal.ZERO) == 0)
            return false;
        contract.pay();
        contract.getCar().setStatus(Car.Status.Available);
        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, contracts);
            System.out.println("Платеж зачислен!");
            return true;
        } catch(IOException e){
            System.out.println("Ошибка записи в файл с договорами: " + e);
            return false;
        }
    }
    private BigDecimal readPayment(BigDecimal requiredPayment){
        Scanner scanner = MainHandler.scanner;
        BigDecimal payment;
        while (true){
            try {
                payment = scanner.nextBigDecimal();
                scanner.nextLine();
                payment = payment.setScale(6, RoundingMode.HALF_UP);
                if (payment.compareTo(BigDecimal.ZERO) != 0 && payment.compareTo(requiredPayment) !=0)
                    throw new IllegalArgumentException("Сумма не равна текущему платежу ");
                break;
            } catch (InputMismatchException | IllegalArgumentException e) {
                if (e.getClass() == InputMismatchException.class)
                    scanner.nextLine();
                System.out.println("Сумма должна быть равна текущему платежу");
            }
        }
        return payment;
    }
    public LeasingContract getContractByID() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер договора или 0 для возврата");
        LeasingContract contract;
        while (true){
            Long contractNumber;
            try {
                contractNumber = Long.valueOf(scanner.nextLine().strip());
                if (contractNumber.equals(0L))
                    return null;
                contract = contracts.stream()
                        .filter(con -> con.getContractNumber().equals(contractNumber))
                        .findFirst().orElse(null);
                if (contract != null)
                    return contract;
                System.out.println("Договор с номером " + contractNumber + " не найден. Попробуйте ещё раз");
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число");
            }
        }
    }
}
