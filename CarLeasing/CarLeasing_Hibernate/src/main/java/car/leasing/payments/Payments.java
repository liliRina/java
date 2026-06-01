package car.leasing.payments;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.contracts.domain.LeasingContract;
import car.leasing.exception.ObjectNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class Payments {
    private final PaymentRepository paymentRepository;

    public Payments(PaymentRepository paymentRepository){
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void addPayments(LeasingContract contract) {
        List<Payment> payments = calculatePayments(contract);
        paymentRepository.saveAll(payments);
    }

    @Transactional
    public void payCurrentPayment(Long contractId){
        paymentRepository.payCurrentPayment(contractId);
    }
    private List<Payment> calculatePayments(LeasingContract contract) {
        List<Payment> payments = new ArrayList<>();
        BigDecimal paymentAmount = contract.getMonthlyPayment();

        for (int i = 1; i <= contract.getMonthsCnt(); i++) {
            payments.add(new Payment(contract.getContractId(), i, paymentAmount, Payment.Status.UNPAID));
        }
        return payments;
    }

    @Transactional
    public boolean isContractClosed(Long contractId) {
        return paymentRepository.findByIdContractId(contractId).stream()
                .allMatch(p -> p.getStatus() == Payment.Status.PAID);
    }

    @Transactional
    public Payment getCurrentPaymentByContract(Long contractId) {
        return paymentRepository.findCurrentPaymentByContractId(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("Payment", "contractId", contractId));
    }
    public List<Payment> getPaymentsByContract(Long contractId){
        return paymentRepository.findByIdContractId(contractId);
    }
}
