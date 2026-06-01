package car.leasing.payments;

import org.springframework.web.bind.annotation.*;
import car.leasing.contracts.Chancellery;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {
    private final Chancellery chancellery;

    public PaymentsController(Chancellery chancellery){
        this.chancellery = chancellery;
    }

    @PostMapping("/{number}")
    public boolean pay(@PathVariable Long number, @RequestParam BigDecimal payment) {
        return chancellery.pay(number, payment);
    }

    @GetMapping("/contract/{number}")
    public List<Payment> getPaymentsById(@PathVariable Long number){
        return chancellery.getPaymentsByIdContracts(number);
    }
}
