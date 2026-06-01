package car.leasing.contracts.domain;

import lombok.Getter;
import car.leasing.cars.domain.CarDTO;
import car.leasing.clients.domain.ClientResponse;

import java.math.BigDecimal;

@Getter
public class ContractResponse {
    private final Long contractId;
    private final CarDTO car;
    private final ClientResponse client;
    private final Integer monthsCnt;
    private final BigDecimal initialPayment;
    private final BigDecimal monthlyPayment;
    private final Double rate;
    private final LeasingContract.Status status;

    public ContractResponse(Long contractId
                            , CarDTO car
                            , ClientResponse client
                            , Integer monthsCnt
                            , BigDecimal initialPayment
                            , BigDecimal monthlyPayment
                            , Double rate
                            , LeasingContract.Status status) {
        this.contractId = contractId;
        this.car = car;
        this.client = client;
        this.monthsCnt = monthsCnt;
        this.initialPayment = initialPayment;
        this.monthlyPayment = monthlyPayment;
        this.rate = rate;
        this.status = status;
    }
}
