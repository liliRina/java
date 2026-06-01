package car.leasing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import car.leasing.clients.ClientsController;

@SpringBootApplication
class LeasingApplication {
    @Autowired
    static ClientsController clientsController;
    public static void main(String[] args) {
        SpringApplication.run(LeasingApplication.class, args);
        InitDB initDB = new InitDB();
    }
}