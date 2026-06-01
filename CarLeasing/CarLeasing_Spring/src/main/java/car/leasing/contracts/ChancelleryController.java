package car.leasing.contracts;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ChancelleryController {
    private final Chancellery chancellery;

    public ChancelleryController(Chancellery chancellery){
        this.chancellery = chancellery;
    }

    @PostMapping("")
    public LeasingContract createContract(@Valid @RequestBody ContractRequest contract) {
        return chancellery.addContract(contract);
    }

    @GetMapping("/search/{id}")
    public LeasingContract getContractById(@PathVariable Long id){
        return chancellery.getContractByID(id);
    }
    @GetMapping("/active/{passport}")
    public List<LeasingContract> getActiveContractsByPassport(@PathVariable String passport){
        return chancellery.getActiveContractsByPassport(passport);
    }
    @GetMapping("/client/{passport}")
    public List<LeasingContract> getCarByPassport(@PathVariable String passport){
        return chancellery.getAllContractsByClient(passport);
    }
}
