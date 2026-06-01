package car.leasing.clients;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.clients.domain.Client;
import car.leasing.exception.DeletionNotAllowedException;
import car.leasing.exception.InvalidParameterException;
import car.leasing.exception.ObjectNotFoundException;

import java.util.List;

@Service
public class Clients {
    private final ClientRepository clientRepository;
    private final RabbitTemplate rabbitTemplate;
    public Clients(ClientRepository clientRepository, RabbitTemplate rabbitTemplate) {
        this.clientRepository = clientRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void register(Client client) {
        if (clientRepository.existsByPassportNumber(client.getPassportNumber()))
            throw new InvalidParameterException("Клиент с паспортом " + client.getPassportNumber() + " уже существует");
        if (clientRepository.existsByPhoneNumber(client.getPhoneNumber()))
            throw new InvalidParameterException("Клиент с телефоном " + client.getPhoneNumber() + " уже существует");
        if (clientRepository.existsByLogin(client.getLogin()))
            throw new InvalidParameterException("Логин " + client.getLogin() + " уже занят");

        rabbitTemplate.convertAndSend("client.ex", "client.add", client,
                new CorrelationData("cохранение клиента с паспортом = " + client.getPassportNumber()));
    }

    public List<Client> getClients() {
        return clientRepository.findAll();
    }
    public Client getClientByID(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Clients", "ID", id));
    }
    public List<Client> getClientsByFullName(String name) {
        if(!Client.checkFullName(name))
            throw new InvalidParameterException("ФИО должно содержать буквы и разделители: пробел и -");
        return clientRepository.findByFullName(name);
    }
    public Client getClientByPassport(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");
        return clientRepository.findByPassportNumber(passportNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", passportNumber));
    }
    public Client getClientByPhone(String phoneNumber) {
        if(!Client.checkPhoneNumber(phoneNumber))
            throw new InvalidParameterException("Телефон должен иметь формат \"+7XXXXXXXXXX\"");
        return clientRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Client", "phoneNumber", phoneNumber));
    }
    public Client getClientByLogin(String login){
        if(login.isBlank())
            throw new InvalidParameterException("Логин не должен быть пустым");
        return clientRepository.findByLogin(login)
                .orElseThrow(() -> new InvalidParameterException("Неправильный пароль и/или логин"));

    }

    public Boolean setClientStatus(String passportNumber, Client.Status status){
        return (Boolean) rabbitTemplate.convertSendAndReceive("client.ex", "client.set_status", status,message -> {
            message.getMessageProperties().setHeader("passportNumber", passportNumber);
            return message;
        }, new CorrelationData("изменение статуса клиента с паспортом = " + passportNumber));
    }

    public void deleteClientByPassport(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");

        Client client = clientRepository.findByPassportNumber(passportNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", passportNumber));
        if (client.getStatus() == Client.Status.NotActiveContract)
            rabbitTemplate.convertAndSend("client.ex", "client.delete", passportNumber,
                    new CorrelationData("удаление клиента с паспортом = " + passportNumber));
        else
            throw new DeletionNotAllowedException("Client", "passportNumber", passportNumber);
    }

    @Transactional
    public Client getClientByPassportForUpdate(String passportNumber) {
        return clientRepository.findByPassportNumberForUpdate(passportNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Clients", "passportNumber", passportNumber));
    }
}
