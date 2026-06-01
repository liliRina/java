package car.leasing.contracts;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import car.leasing.contracts.domain.ContractRequest;
import car.leasing.contracts.domain.ContractResponse;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ChancelleryController {
    private final Chancellery chancellery;

    public ChancelleryController(Chancellery chancellery){
        this.chancellery = chancellery;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN') or @chancellery.isOwner(#contract.getClientPassport(), authentication.name)")
    public ResponseEntity createContract(@Valid @RequestBody ContractRequest contract) {
        chancellery.createContract(contract);
        return ResponseEntity
                .accepted()
                .body("Договор отправлен на обработку");
    }

    @GetMapping("/search/{id}")
    @PreAuthorize("hasRole('ADMIN') or @chancellery.isOwnerById(#id, authentication.name)")
    public ContractResponse getContractById(@PathVariable Long id){
        return chancellery.createContractResponse(chancellery.getContractByID(id));
    }

    @GetMapping("/active/{passport}")
    @PreAuthorize("hasRole('ADMIN') or @chancellery.isOwner(#passport, authentication.name)")
    public List<ContractResponse> getActiveContractsByPassport(@PathVariable String passport){
        return chancellery.getActiveContractsByPassport(passport);
    }
    @GetMapping("/client/{passport}")
    @PreAuthorize("hasRole('ADMIN') or @chancellery.isOwner(#passport, authentication.name)")
    public List<ContractResponse> getHistoryByPassport(@PathVariable String passport){
        return chancellery.getAllContractsByClient(passport);
    }
}
