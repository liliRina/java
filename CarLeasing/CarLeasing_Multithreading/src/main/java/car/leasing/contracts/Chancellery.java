package car.leasing.contracts;

import car.leasing.MainHandler;
import car.leasing.cars.Car;
import car.leasing.clients.Client;
import car.leasing.clients.ClientBase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;

import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Chancellery {
    private CopyOnWriteArrayList<LeasingContract> contracts = new CopyOnWriteArrayList<>();
    private ContractsIO contractsIO;
    private AtomicInteger cntContractsInProcess = new AtomicInteger(0);

    public Chancellery() {
        contractsIO = new ContractsIO();
        contractsIO.readContracts(contracts);
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

        Long newID = contracts.size() != 0 ? contracts.getLast().getContractNumber() + 1 : 1;
        LeasingContract contract;
        try{
            contract = new LeasingContract(newID, car, client,
                    cntMonths, initialPayment, rate);
        } catch (LeasingContractCreateException e){
            System.out.println("Ошибка создания договора: " + e);
            return false;
        }
        System.out.println("Договор принят");
        contractsIO.saveNewContract(contracts, contract);
        return true;
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
                System.out.println("Введён первоначальный платёж " + initialPayment);
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

    public BigDecimal getUserPayment(LeasingContract contract){
        if(!contract.tryStartPayment()) {
            System.out.println("По этому договору уже обрабатывается платёж, подождите");
            return BigDecimal.ZERO;
        }
        cntContractsInProcess.incrementAndGet();
        if (contract.getStatus() == LeasingContract.Status.CLOSED){
            System.out.println("Договор уже закрыт");
            finishContractProcess(contract);
            return BigDecimal.ZERO;
        }
        contract.showCurPayment();
        System.out.println("Введите сумму или 0 для возврата");
        BigDecimal cost = readPayment();
        System.out.println("Платёж принят");
        return cost;
    }

    public boolean pay(LeasingContract contract) {
        Future<Boolean> f = contractsIO.payContract(contracts, contract);
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }
    public boolean returnPay(LeasingContract contract) {
        Future<Boolean> f = contractsIO.returnPayContract(contracts, contract);
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    public void finishContractProcess(LeasingContract contract) {
        contract.finishProcess();
        cntContractsInProcess.decrementAndGet();
    }

    private BigDecimal readPayment(){
        Scanner scanner = MainHandler.scanner;
        BigDecimal payment;
        while (true){
            try {
                payment = scanner.nextBigDecimal();
                scanner.nextLine();
                payment = payment.setScale(6, RoundingMode.HALF_UP);
                if (payment.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Сумма должна быть положительной");
                break;
            } catch (InputMismatchException | IllegalArgumentException e) {
                if (e.getClass() == InputMismatchException.class)
                    scanner.nextLine();
                System.out.println("Сумма должна быть положительным число");
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

    public boolean isAvailableForPayments() {
        return contractsIO.isAvailable(); // нет добавленных, но не обработанных договоров
    }
    public boolean isAvailable(){ // нет неоплаченных платежей и необработанных добавлннных договоров
        return cntContractsInProcess.get() == 0 && contractsIO.isAvailable();
    }

    public void close() {
        contractsIO.finish();
    }
}
