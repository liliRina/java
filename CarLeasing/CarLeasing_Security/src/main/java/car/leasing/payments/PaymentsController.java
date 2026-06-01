package car.leasing.payments;

import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN') or @chancellery.isOwnerById(#number, authentication.name)")
    public boolean pay(@PathVariable Long number, @RequestParam BigDecimal payment) {
        return chancellery.pay(number, payment);
    }

    @GetMapping("/contract/{number}")
    @PreAuthorize("hasRole('ADMIN') or @chancellery.isOwnerById(#number, authentication.name)")
    public List<Payment> getPaymentsById(@PathVariable Long number){
        return chancellery.getPaymentsByIdContracts(number);
    }
}
