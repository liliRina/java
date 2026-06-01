package car.leasing.payments;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RabbitListener(queues = "paymentQueue")
@AllArgsConstructor
public class PaymentMessageHandler {
    private final PaymentRepository paymentRepository;
    private static final Logger log = LoggerFactory.getLogger(PaymentMessageHandler.class);

    @RabbitHandler
    @SendTo("")
    public Boolean handleAdd(String amountStr, @Header("monthsCnt") Integer cntMonth, @Header("contractId") Long id) {
        List<Payment> payments = new ArrayList<>();
        BigDecimal amount = new BigDecimal(amountStr);
        for (int i = 1; i <= cntMonth; i++) {
            payments.add(new Payment(id, i, amount, Payment.Status.UNPAID));
        }
        try {
            paymentRepository.saveAll(payments);
            return Boolean.TRUE;
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при сохранении платежей: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitHandler
    @Transactional
    public void handlePay(Long contractId) {
        try {
            paymentRepository.payCurrentPayment(contractId);
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при оплате платежа: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
