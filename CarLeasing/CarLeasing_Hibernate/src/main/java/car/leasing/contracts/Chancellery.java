package car.leasing.contracts;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import car.leasing.cars.domain.Car;
import car.leasing.cars.domain.CarDTO;
import car.leasing.cars.Garage;
import car.leasing.clients.domain.Client;
import car.leasing.clients.domain.ClientResponse;
import car.leasing.clients.Clients;
import car.leasing.contracts.domain.ContractRequest;
import car.leasing.contracts.domain.ContractResponse;
import car.leasing.contracts.domain.LeasingContract;
import car.leasing.exception.InvalidParameterException;
import car.leasing.exception.ObjectNotFoundException;
import car.leasing.exception.PayClosedContractException;
import car.leasing.payments.Payment;
import car.leasing.payments.Payments;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class Chancellery {
    private final CopyOnWriteArrayList<LeasingContract> contracts = new CopyOnWriteArrayList<>();
    private final ContractsDB contractsDB;
    private final ContractRepository contractRepository;
    private final Payments payments;
    private final Garage garage;
    private final Clients clients;

    public Chancellery(ContractsDB contractsDB
            , ContractRepository contractRepository
            , Garage garage
            , Clients clients
            , Payments payments) {
        this.contractsDB = contractsDB;
        this.contractRepository = contractRepository;
        this.garage = garage;
        this.clients = clients;
        this.payments = payments;
        rewriteContracts();
    }

    @Transactional
    public ContractResponse addContract(ContractRequest requestContract) {
        LeasingContract contract = createContract(requestContract);
        garage.setStatus(contract.getCar().getVIN(), Car.Status.InUse);
        clients.setClientStatus(contract.getClient().getPassportNumber(), Client.Status.HasActiveContract);
        payments.addPayments(contract);
        contractsDB.saveNewContract(contracts, contract);
        return createContractResponse(contract);
    }

    private LeasingContract createContract(ContractRequest contract){ // не надо транзакции, всё и так в 1 будет
        Car car = garage.getCarByVinForUpdate(contract.getCarVin());
        if (car.getStatus() == Car.Status.InUse)
            throw new InvalidParameterException("Машина занята");

        Client client = clients.getClientByPassportForUpdate(contract.getClientPassport());

        if (car.getPrice().compareTo(contract.getInitialPayment()) < 0)
            throw new InvalidParameterException("Начальный платёж должен быть меньше стоимости автомобиля");
        LeasingContract Lcontract = new LeasingContract(null, car, client
                , contract.getMonthsCnt(), contract.getInitialPayment(), contract.getRate());
        return contractRepository.save(Lcontract);
    }

    @Transactional
    public boolean pay(Long id, BigDecimal payment) {
        LeasingContract contract = getContractByID(id);
        if (contract.getStatus() == LeasingContract.Status.CLOSED)
            throw new PayClosedContractException(id);

        if (payment.compareTo(getCurrentPayment(id).getPayment()) != 0)
            throw new InvalidParameterException("Внесённый платёж не соответствует требуемой сумме");

        payments.payCurrentPayment(contract.getContractId());
        if (payments.isContractClosed(contract.getContractId())){
            contractRepository.updateStatus(contract.getContractId(), LeasingContract.Status.CLOSED);
            garage.setStatus(contract.getCar().getVIN(), Car.Status.Available);
            String passportNumber = contract.getClient().getPassportNumber();
            Client client = clients.getClientByPassportForUpdate(passportNumber);
            if (!hasActiveContractByClientPassportExCur(passportNumber, contract))
                client.setStatus(Client.Status.NotActiveContract);
            contractsDB.setClosedStatus(contracts, contract);
        }
        return true;
    }

    public LeasingContract getContractByID(Long id) {
        if (id == null || id < 0)
            throw new InvalidParameterException("Номер договора должен быть положительным числом");
        return contracts.stream()
                .filter(con -> con.getContractId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ObjectNotFoundException("Contract", "ID", id));
    }
    public List<ContractResponse> getAllContractsByClient(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");
        return contracts.stream()
                .filter(con -> con.getClient().getPassportNumber().equals(passportNumber))
                .map(this::createContractResponse)
                .toList();
    }
    public List<ContractResponse> getActiveContractsByPassport(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");
        return contracts.stream()
                .filter(con -> con.getClient().getPassportNumber().equals(passportNumber) &&
                        con.getStatus() == LeasingContract.Status.ACTIVE)
                .map(this::createContractResponse)
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

    public boolean hasActiveContractByClientPassportExCur(String passportNumber, LeasingContract contract){
        return contracts.stream()
                .anyMatch(con -> con.getClient().getPassportNumber().equals(passportNumber) &&
                                con.getStatus() == LeasingContract.Status.ACTIVE && !con.getContractId().equals(contract.getContractId()));
    }
    public ContractResponse createContractResponse(LeasingContract contract) {
        CarDTO carDTO = new CarDTO(contract.getCar().getVIN()
                                , contract.getCar().getBrand()
                                , contract.getCar().getModel()
                                , contract.getCar().getYear()
                                , contract.getCar().getPrice());

        ClientResponse clientResponse = new ClientResponse(contract.getClient().getID()
                , contract.getClient().getFullName()
                , contract.getClient().getPassportNumber()
                , contract.getClient().getPhoneNumber());

        return new ContractResponse(contract.getContractId()
                                    , carDTO
                                    , clientResponse
                                    , contract.getMonthsCnt()
                                    , contract.getInitialPayment()
                                    , contract.getMonthlyPayment()
                                    , contract.getRate()
                                    , contract.getStatus()
        );
    }
    public void rewriteContracts(){
        List<LeasingContract> DBcontracts = contractRepository.findAll();
        contracts.clear();
        contracts.addAll(DBcontracts);
        contractsDB.updateFile(contracts);
    }
}