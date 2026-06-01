package car.leasing.clients;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import car.leasing.exception.DeletionNotAllowedException;
import car.leasing.exception.InvalidParameterException;
import car.leasing.exception.ObjectNotFoundException;

import java.util.List;

@Service
public class Clients {
    private final ClientsDB clientsDB;
    public Clients(ClientsDB clientsDB) { this.clientsDB = clientsDB; }

    public Client addClient(Client client) {
        return clientsDB.saveNewClient(client);
    }

    public List<Client> getClients() {
        return clientsDB.getClients();
    }
    public Client getClientByID(Long id) {
        return clientsDB.getClientById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Clients", "ID", id));
    }
    public List<Client> getClientsByFullName(String name) {
        if(!Client.checkFullName(name))
            throw new InvalidParameterException("ФИО должно содержать буквы и разделители: пробел и -");
        return clientsDB.getClientsByFullName(name);
    }
    public Client getClientByPassport(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");
        return clientsDB.getClientByPassportNumber(passportNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", passportNumber));
    }
    public Client getClientByPhone(String phoneNumber) {
        if(!Client.checkPhoneNumber(phoneNumber))
            throw new InvalidParameterException("Телефон должен иметь формат \"+7XXXXXXXXXX\"");
        return clientsDB.getClientByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Client", "phoneNumber", phoneNumber));
    }

    @Transactional
    public void setClientStatus(String passportNumber, Client.Status status){
        clientsDB.updateClientStatus(passportNumber, status);
    }

    @Transactional
    public Client deleteClientByPassport(String passportNumber) {
        if(!Client.checkPassportNumber(passportNumber))
            throw new InvalidParameterException("Паспорт должен состоять из 10 цифр");

        Client client = clientsDB.getClientByPassportForUpdate(passportNumber)
                .orElseThrow(() -> new ObjectNotFoundException("Client", "passportNumber", passportNumber));
        if (client.getStatus() == Client.Status.NotActiveContract)
            clientsDB.deleteClient(passportNumber);
        else
            throw new DeletionNotAllowedException("Client", "passportNumber", passportNumber);
        return client;
    }
}
