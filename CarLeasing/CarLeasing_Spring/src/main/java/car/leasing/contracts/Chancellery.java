package car.leasing.contracts;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import car.leasing.cars.Car;
import car.leasing.cars.Garage;
import car.leasing.clients.Client;
import car.leasing.clients.Clients;
import car.leasing.exception.InvalidParameterException;
import car.leasing.exception.ObjectNotFoundException;
import car.leasing.exception.PayClosedContractException;
import car.leasing.payments.Payment;
import car.leasing.payments.Payments;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Chancellery {
    private final CopyOnWriteArrayList<LeasingContract> contracts = new CopyOnWriteArrayList<>();
    private final ContractsDB contractsDB;
    private final Payments payments;
    private final Garage garage;
    private final Clients clients;
    private AtomicLong cntContracts = new AtomicLong();

    public Chancellery(ContractsDB contractsDB
            , Garage garage
            , Clients clients
            , Payments payments) {
        this.contractsDB = contractsDB;
        this.garage = garage;
        this.clients = clients;
        this.payments = payments;
        contractsDB.readContracts(contracts);
        cntContracts.set(!contracts.isEmpty() ? contracts.get(contracts.size() - 1).getContractNumber() : 0L);
    }

    @Transactional
    public LeasingContract addContract(ContractRequest requestContract) {

        LeasingContract contract = createContract(requestContract);
        garage.setStatus(contract.getCar().getVIN(), Car.Status.InUse);
        clients.setClientStatus(contract.getClient().getPassportNumber(), Client.Status.HasActiveContract);
        payments.addPayments(contract);
        contractsDB.saveNewContract(contracts, contract);
        cntContracts.getAndIncrement();
        return contract;
    }

    private LeasingContract createContract(ContractRequest contract){
        Car car = garage.getCarByVin(contract.getCarVin());
        if (car.getStatus() == Car.Status.InUse)
            throw new InvalidParameterException("Машина занята");

        Client client = clients.getClientByPassport(contract.getClientPassport());
        if (car.getPrice().compareTo(contract.getInitialPayment()) < 0)
            throw new InvalidParameterException("Начальный платёж должен быть меньше стоимости автомобиля");
        return new LeasingContract(cntContracts.get() + 1
                , car
                , client
                , contract.getMonthsCnt()
                , contract.getInitialPayment()
                , contract.getRate());
    }

    @Transactional
    public boolean pay(Long id, BigDecimal payment) {
        LeasingContract contract = getContractByID(id);
        if (contract.getStatus() == LeasingContract.Status.CLOSED)
            throw new PayClosedContractException(id);

        if (payment.compareTo(getCurrentPayment(id).getPayment()) != 0)
            throw new InvalidParameterException("Внесённый платёж не соответствует требуемой сумме");

        payments.payCurrentPayment(contract.getContractNumber());
        if (payments.isContractClosed(contract.getContractNumber())){
            contractsDB.setClosedStatus(contracts, contract);
            garage.setStatus(contract.getCar().getVIN(), Car.Status.Available);
            String passportNumber = contract.getClient().getPassportNumber();
            if (!hasActiveContractByClientPassport(passportNumber))
                clients.setClientStatus(passportNumber, Client.Status.NotActiveContract);
        }
        return true;
    }

    public LeasingContract getContractByID(Long id) {
        if (id == null || id < 0)
            throw new InvalidParameterException("Номер договора должен быть положительным числом");
        return contracts.stream()
                .filter(con -> con.getContractNumber().equals(id))
                .findFirst()
                .orElseThrow(() -> new ObjectNotFoundException("Contract", "ID", id));
    }
    public List<LeasingContract> getAllContractsByClient(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");
        return contracts.stream()
                .filter(con -> con.getClient().getPassportNumber().equals(passportNumber))
                .toList();
    }
    public List<LeasingContract> getActiveContractsByPassport(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");
        return contracts.stream()
                .filter(con -> con.getClient().getPassportNumber().equals(passportNumber) &&
                        con.getStatus() == LeasingContract.Status.ACTIVE)
                .toList();
    }

    public Payment getCurrentPayment(Long id){
        if (id == null || id < 0)
            throw new InvalidParameterException("Номер договора должен быть положительным числом");
        return payments.getCurrentPaymentByContract(id);
    }

    public List<Payment> getPaymentsByIdContracts(Long contractId){
        if (contractId == null || contractId < 0)
            throw new InvalidParameterException("Номер договора должен быть положительным числом");

        return payments.getPaymentsByContract(contractId);
    }

    public boolean hasActiveContractByClientPassport(String passportNumber){
        return contracts.stream()
                .anyMatch(con -> con.getClient().getPassportNumber().equals(passportNumber) &&
                                con.getStatus() == LeasingContract.Status.ACTIVE);
    }
}