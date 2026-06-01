package car.leasing.contracts;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.cars.CarRepository;
import car.leasing.cars.domain.Car;
import car.leasing.clients.ClientRepository;
import car.leasing.clients.domain.Client;
import car.leasing.contracts.domain.ContractRequest;
import car.leasing.contracts.domain.LeasingContract;
import car.leasing.exception.ObjectNotFoundException;

@Slf4j
@Component
@RabbitListener(queues = "contractQueue")
@AllArgsConstructor
public class ContractMessageHandler {

    private final ContractRepository contractRepository;
    private final CarRepository carRepository;
    private final ClientRepository clientRepository;

    @RabbitHandler
    @Transactional
    @SendTo("")
    public LeasingContract handleAdd(ContractRequest contractRequest){
        try{
            Car car = carRepository.findByIdForUpdate(contractRequest.getCarVin())
                    .orElseThrow(() -> new ObjectNotFoundException("Car", "VIN", contractRequest.getCarVin()));
            Client client = clientRepository.findByPassportNumberForUpdate(contractRequest.getClientPassport())
                    .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", contractRequest.getClientPassport()));

            LeasingContract contract = new LeasingContract(null, car, client
                    , contractRequest.getMonthsCnt(), contractRequest.getInitialPayment(), contractRequest.getRate());
            return contractRepository.save(contract);
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при сохранении договора: ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
    @RabbitHandler
    @Transactional
    public void handleClose(Long id){
        try {
            contractRepository.updateStatus(id, LeasingContract.Status.CLOSED);
        } catch (NonTransientDataAccessException e) {
            log.error("Ошибка в бд при закрывании договора ", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
