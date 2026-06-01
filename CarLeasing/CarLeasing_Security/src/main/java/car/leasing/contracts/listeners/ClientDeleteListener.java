package car.leasing.contracts.listeners;

import jakarta.persistence.PostRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import car.leasing.clients.domain.Client;
import car.leasing.contracts.Chancellery;

@Component
public class ClientDeleteListener {
    private static Chancellery chancellery;

    @Autowired
    public void setChancellery(Chancellery chancellery) {
        ClientDeleteListener.chancellery = chancellery;
    }

    @PostRemove
    public void onCarDelete(Client deletedClient) {
        chancellery.rewriteContracts();
        System.out.println("JSON файл обновлен после удаления машины с VIN: " + deletedClient.getID());
    }
}
