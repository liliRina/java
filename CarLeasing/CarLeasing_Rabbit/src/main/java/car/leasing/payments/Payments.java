package car.leasing.payments;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import car.leasing.contracts.domain.LeasingContract;
import car.leasing.exception.ObjectNotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
public class Payments {
    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    public Boolean addPayments(LeasingContract contract) {
        return (Boolean) rabbitTemplate.convertSendAndReceive("payment.ex", "payment.add", contract.getMonthlyPayment().toString(), message -> {
            message.getMessageProperties().setHeader("monthsCnt", contract.getMonthsCnt());
            message.getMessageProperties().setHeader("contractId", contract.getContractId());
            return message;
        }, new CorrelationData("добавление платежей по договору №" + contract.getContractId()));
    }

    public void payCurrentPayment(Long contractId){
        rabbitTemplate.convertSendAndReceive("payment.ex", "payment.pay", contractId
                ,new CorrelationData("оплата платежа по договору №" + contractId));
    }

    public boolean isContractClosed(Long contractId) {
        return paymentRepository.findByIdContractId(contractId).stream()
                .allMatch(p -> p.getStatus() == Payment.Status.PAID);
    }
    public void deletePayments(Long contractId){
        paymentRepository.deleteByContractId(contractId);
    }

    public Payment getCurrentPaymentByContract(Long contractId) {
        return paymentRepository.findCurrentPaymentByContractId(contractId)
                .orElseThrow(() -> new ObjectNotFoundException("Payment", "contractId", contractId));
    }
    public List<Payment> getPaymentsByContract(Long contractId){
        return paymentRepository.findByIdContractId(contractId);
    }
}
