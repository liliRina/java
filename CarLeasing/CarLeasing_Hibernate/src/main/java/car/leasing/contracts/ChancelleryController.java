package car.leasing.contracts;

import jakarta.validation.Valid;
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
    public ContractResponse createContract(@Valid @RequestBody ContractRequest contract) {
        return chancellery.addContract(contract);
    }

    @GetMapping("/search/{id}")
    public ContractResponse getContractById(@PathVariable Long id){
        return chancellery.createContractResponse(chancellery.getContractByID(id));
    }
    @GetMapping("/active/{passport}")
    public List<ContractResponse> getActiveContractsByPassport(@PathVariable String passport){
        return chancellery.getActiveContractsByPassport(passport);
    }
    @GetMapping("/client/{passport}")
    public List<ContractResponse> getCarByPassport(@PathVariable String passport){
        return chancellery.getAllContractsByClient(passport);
    }
}
