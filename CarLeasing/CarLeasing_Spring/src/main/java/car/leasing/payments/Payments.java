package car.leasing.payments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.contracts.LeasingContract;
import car.leasing.exception.ObjectNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class Payments {
    private final PaymentsDB paymentsDB;

    public Payments(PaymentsDB paymentsDB){
        this.paymentsDB = paymentsDB;
    }

    @Transactional
    public void addPayments(LeasingContract contract) {
        List<Payment> payments = calculatePayments(contract);
        paymentsDB.savePaymentsBatch(contract.getContractNumber(), payments);
    }

    @Transactional
    public void payCurrentPayment(Long contractId){
        paymentsDB.payCurrentPayment(contractId);
    }
    private List<Payment> calculatePayments(LeasingContract contract) {
        List<Payment> payments = new ArrayList<>();

        BigDecimal loan = contract.getCar().getPrice().subtract(contract.getInitialPayment());
        BigDecimal monthRate = BigDecimal.valueOf(contract.getRate())
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        BigDecimal paymentAmount = loan.multiply(monthRate)
                .multiply(monthRate.add(BigDecimal.ONE).pow(contract.getMonthsCnt()))
                .divide(monthRate.add(BigDecimal.ONE).pow(contract.getMonthsCnt())
                        .subtract(BigDecimal.ONE), 6, RoundingMode.HALF_UP);

        for (int i = 1; i <= contract.getMonthsCnt(); i++) {
            payments.add(new Payment(contract.getContractNumber(), i, paymentAmount, Payment.Status.UNPAID));
        }
        return payments;
    }

    @Transactional
    public boolean isContractClosed(Long contractId) {
        return paymentsDB.getPaymentsByContractId(contractId).stream()
                .allMatch(p -> p.getStatus() == Payment.Status.PAID);
    }

    @Transactional
    public Payment getCurrentPaymentByContract(Long contractId) {
        return paymentsDB.getCurrentPaymentByContract(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("Payment", "contractId", contractId));
    }
    public List<Payment> getPaymentsByContract(Long contractId){
        return paymentsDB.getPaymentsByContractId(contractId);
    }
}
