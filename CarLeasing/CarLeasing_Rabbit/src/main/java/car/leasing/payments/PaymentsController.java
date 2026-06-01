package car.leasing.payments;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> pay(@PathVariable Long number, @RequestParam BigDecimal payment) {
        chancellery.pay(number, payment);
        return ResponseEntity
                .accepted()
                .body("Платёж отправлен на обработку");
    }

    @GetMapping("/contract/{number}")
    @PreAuthorize("hasRole('ADMIN') or @chancellery.isOwnerById(#number, authentication.name)")
    public List<Payment> getPaymentsById(@PathVariable Long number){
        return chancellery.getPaymentsByIdContracts(number);
    }
}
