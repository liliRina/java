package car.leasing.clients;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import car.leasing.clients.domain.Client;
import car.leasing.exception.ObjectNotFoundException;

@Component
@RabbitListener(queues = "clientQueue")
@AllArgsConstructor
public class ClientMessageHandler {
    public final ClientRepository clientRepository;
    private static final Logger log = LoggerFactory.getLogger(ClientMessageHandler.class);

    @RabbitHandler
    public void handleAdd(Client client){
        try{
            clientRepository.save(client);
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при сохранении клиента: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitHandler
    @Transactional
    @SendTo("")
    public Boolean handleSetStatus(@Payload Client.Status status, @Header("passportNumber") String passportNumber){
        try {
            Client client = clientRepository.findByPassportNumberForUpdate(passportNumber)
                    .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", passportNumber));
            client.setStatus(status);
            return Boolean.TRUE;
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при изменении статуса клиента: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
    @RabbitHandler
    @Transactional
    public void handleDelete(String passportNumber){
        try {
            Client client = clientRepository.findByPassportNumberForUpdate(passportNumber)
                    .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", passportNumber));
            if (client.getStatus() == Client.Status.NotActiveContract)
                clientRepository.deleteByPassportAndStatusNot(passportNumber);
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при удалении клиента: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
