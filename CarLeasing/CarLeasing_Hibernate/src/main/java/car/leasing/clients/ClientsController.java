package car.leasing.clients;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import car.leasing.clients.domain.Client;
import car.leasing.clients.domain.ClientDTO;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientsController {
    private final Clients clients;

    public ClientsController(Clients clients){
        this.clients = clients;
    }

    @PostMapping("")
    public Client createClient(@Valid @RequestBody ClientDTO clientDTO) {
        Client client = new Client(clientDTO.getFullName(), clientDTO.getPassportNumber(), clientDTO.getPhoneNumber());
        return clients.addClient(client);
    }

    @GetMapping("")
    public List<Client> getClients(){
        return clients.getClients();
    }
    @GetMapping("/search/id/{id}")
    public Client getClientById(@PathVariable Long id){
        return clients.getClientByID(id);
    }
    @GetMapping("/search/full_name/{full_name}")
    public List<Client> getClientByFullName(@PathVariable String full_name){
        return clients.getClientsByFullName(full_name.strip());
    }
    @GetMapping("/search/passport_number/{passport_number}")
    public Client getCarByPassport(@PathVariable String passport_number){
        return clients.getClientByPassport(passport_number.strip());
    }
    @GetMapping("/search/phone_number/{phone_number}")
    public Client getCarByPhone(@PathVariable String phone_number){
        return clients.getClientByPhone(phone_number.strip());
    }

    @DeleteMapping("client/{passport_number}")
    public Client deleteClientByPassport(@PathVariable String passport_number) {
        return clients.deleteClientByPassport(passport_number.strip());
    }
}
