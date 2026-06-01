package car.leasing.contracts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import car.leasing.MainHandler;
import car.leasing.cars.Car;
import car.leasing.clients.Client;
import car.leasing.clients.Clients;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;

import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Chancellery {
    private final CopyOnWriteArrayList<LeasingContract> contracts = new CopyOnWriteArrayList<>();
    private final ContractsDB contractsDB;
    private final AtomicInteger cntContractsInProcess = new AtomicInteger(0);
    private static final Logger log = LoggerFactory.getLogger(Chancellery.class);

    public Chancellery() {
        contractsDB = new ContractsDB();
        contractsDB.readContracts(contracts);
    }

    public boolean processingAddContract(Car car, Client client, Runnable onError) {
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

        Integer newID = !contracts.isEmpty() ? contracts.getLast().getContractNumber() + 1 : 1;
        LeasingContract contract;
        try{
            contract = new LeasingContract(newID, car, client,
                    cntMonths, initialPayment, rate);
        } catch (LeasingContractCreateException e){
            System.out.println("Ошибка при создании договора");
            return false;
        }
        System.out.println("Договор принят");

        contractsDB.saveNewContract(contracts, contract)
            .thenAccept(success ->{
                if (success)
                    System.out.println("Договор успешно добавлен!");
                else{
                    System.out.println("Не удалось добавить договор");
                    onError.run();
                }
            });
        return true;
    }

    public void showContractByID() {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер договора или 0 для возврата");
        while (true){
            Integer contractNumber;
            try {
                String input = scanner.nextLine().strip();
                log.info("Введён id: {}", input);
                contractNumber = Integer.valueOf(input);
                if (contractNumber.equals(0))
                    return;
                System.out.println("ДОГОВОР с номером " + contractNumber +":");
                LeasingContract contract = contracts.stream()
                        .filter(con -> con.getContractNumber().equals(contractNumber))
                        .findFirst().orElse(null);
                if (contract == null)
                    break;
                System.out.println(contract + "\n" + "Платежи: ");
                contractsDB.getPayments(contract)
                        .forEach(System.out::println);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число");
            }
        }
    }
    public void showContractsByClient(Clients clients) {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер телефона клиента или 0 для возврата");
        String phoneNumber = clients.readPhoneNumber(scanner);
        if (phoneNumber.isEmpty())
            return;
        System.out.println("ИСТОРИЯ ДОГОВОРОВ клиента с номером телефона " + phoneNumber +":");
        contracts.stream()
                .filter(con -> con.getClient().getPhoneNumber().equals(phoneNumber))
                .forEach(c -> {
                        System.out.println(c + "\n" + "Платежи: ");
                        contractsDB.getPayments(c)
                            .forEach(System.out::println);
                });
    }
    public void showActiveContractsByClient(Clients clients) {
        Scanner scanner = MainHandler.scanner;
        System.out.println("Введите номер телефона клиента или 0 для возврата");
        String phoneNumber = clients.readPhoneNumber(scanner);
        if (phoneNumber.isEmpty())
            return;
        System.out.println("АКТИВНЫЕ ДОГОВОРЫ клиента с номером телефона " + phoneNumber +":");
        contracts.stream()
                .filter(con -> con.getClient().getPhoneNumber().equals(phoneNumber) &&
                        con.getStatus() == LeasingContract.Status.ACTIVE)
                .forEach(c -> {
                    System.out.println(c + "\n" + "Платежи: ");
                    contractsDB.getPayments(c)
                            .forEach(System.out::println);
                });
    }
    public boolean hasActiveContractsByClient(Client client) {
        String phoneNumber = client.getPhoneNumber();
        return contracts.stream()
                .anyMatch(con -> con.getClient().getPhoneNumber().equals(phoneNumber) &&
                        con.getStatus() == LeasingContract.Status.ACTIVE);
    }

    private BigDecimal readInitialPayment(Scanner scanner, BigDecimal carPrice){
        BigDecimal initialPayment;
        while (true){
            try {
                String input = scanner.nextLine().strip();
                log.info("Введён начальный платёж: {}", input);
                initialPayment = new BigDecimal(input);
                initialPayment = initialPayment.setScale(6, RoundingMode.HALF_UP);
                if(initialPayment.compareTo(carPrice) >= 0
                        || initialPayment.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Первоначальный взнос больше стоимости машины или отрицательный");
                break;
            } catch (InputMismatchException | IllegalArgumentException e) {
                System.out.println("Введите положительное число меньше стоимости машины: " + carPrice);
            }
        }
        return initialPayment;
    }
    private Integer readCntMonth(Scanner scanner){
        Integer cntMonths;
        while(true) {
            try {
                String input = scanner.nextLine().strip();
                log.info("Введено количество месяцев: {}", input);
                cntMonths = Integer.valueOf(input);
                if (cntMonths == 0)
                    return 0;
                if(!LeasingContract.checkCntMonth(cntMonths))
                    throw new IllegalArgumentException("Количество месяцев или равно 0");
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Введите положительное целое число");
            }
        }
        return cntMonths;
    }
    private Double readRate(Scanner scanner){
        Double rate;
        while (true){
            try {
                String input = scanner.nextLine().strip();
                log.info("Введена процентная ставка: {}", input);
                rate = Double.valueOf(input);
                if (Math.abs(rate) < 0.000001)
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

        Payment payment = getCurrentPayment(contract);
        if (payment == null){
            finishContractProcess(contract);
            return BigDecimal.ZERO;
        }
        System.out.println("Текущий платёж: " +
                payment.getPayment() +
                " (месяц " + payment.getNumber() + ")");

        System.out.println("Введите сумму или 0 для возврата");
        BigDecimal cost = readPayment();
        if (cost.compareTo(BigDecimal.ZERO) != 0)
            System.out.println("Платёж принят");
        else
            finishContractProcess(contract);
        return cost;
    }
    public Payment getCurrentPayment(LeasingContract contract){
        return contractsDB.getCurrentPaymentByContract(contract);
    }

    public boolean pay(LeasingContract contract) {
        Future<Boolean> f = contractsDB.payContract(contracts, contract);
        try {
            if (f.get()){
                System.out.println("Платёж внесён!");
                if (contract.getStatus() == LeasingContract.Status.CLOSED)
                    System.out.println("Договор закрыт!");
                return true;
            }
            else{
                System.out.println("Не удалось внести платёж: платёж не найден или произошла ошибка оплаты");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Ошибка при оплате платежа по контракту №{}:", contract.getContractNumber(), e);
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
                String input = scanner.nextLine().strip();
                log.info("Введён вносимый платёж: {}", input );
                payment = new BigDecimal(input);
                payment = payment.setScale(6, RoundingMode.HALF_UP);
                if (payment.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Сумма должна быть положительной");
                break;
            } catch (InputMismatchException | IllegalArgumentException e) {
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
            Integer contractNumber;
            try {
                String input = scanner.nextLine().strip();
                log.info("Введён номер договора: {}", input);
                contractNumber = Integer.valueOf(input);
                if (contractNumber.equals(0))
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

    public boolean hasActiveContractByClientPassport(Client client){
        return contracts.stream()
                .anyMatch(con -> con.getClient().getPassportNumber().equals(client.getPassportNumber()) &&
                                con.getStatus() == LeasingContract.Status.ACTIVE);
    }

    public boolean isAvailableForPayments() {
        return contractsDB.isAvailable(); // нет добавленных, но не обработанных договоров
    }
    public boolean isAvailable(){ // нет неоплаченных платежей и необработанных добавлннных договоров
        return cntContractsInProcess.get() == 0 && contractsDB.isAvailable();
    }

    public void close() {
        contractsDB.finish();
    }
}

//    public boolean returnPay(LeasingContract contract) {
//        Future<Boolean> f = contractsDB.returnPayContract(contracts, contract);
//        try {
//            return f.get();
//        } catch (InterruptedException | ExecutionException e) {
//            return false;
//        }
//    }
