package car.leasing.clients;

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
    public Clients(ClientRepository clientRepository) { this.clientRepository = clientRepository; }

    public Client addClient(Client client) {
        if (clientRepository.existsByPassportNumber(client.getPassportNumber()))
            throw new InvalidParameterException("Клиент с паспортом " + client.getPassportNumber() + " уже существует");

        if (clientRepository.existsByPhoneNumber(client.getPhoneNumber()))
            throw new InvalidParameterException("Клиент с телефоном " + client.getPhoneNumber() + " уже существует");

        return clientRepository.save(client);
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

    @Transactional
    public void setClientStatus(String passportNumber, Client.Status status){
        clientRepository.updateStatus(passportNumber, status);
    }

    @Transactional
    public Client deleteClientByPassport(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");

        Client client = clientRepository.findByPassportNumberForUpdate(passportNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", passportNumber));
        if (client.getStatus() == Client.Status.NotActiveContract)
            clientRepository.deleteByPassportAndStatusNot(passportNumber);
        else
            throw new DeletionNotAllowedException("Client", "passportNumber", passportNumber);
        return client;
    }

    @Transactional
    public Client getClientByPassportForUpdate(String passportNumber) {
        return clientRepository.findByPassportNumberForUpdate(passportNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Clients", "passportNumber", passportNumber));
    }
}
