package car.leasing.contracts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.stereotype.Component;
import car.leasing.contracts.domain.LeasingContract;
import car.leasing.exception.FileException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class ContractsDB {
    private final File file = new File("src/main/resources/contracts.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public void readContracts(CopyOnWriteArrayList<LeasingContract> contracts) {
        try{
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                new ObjectMapper().writeValue(file, new ArrayList<>());
            }
            else{
                contracts.addAll(mapper.readValue(file, new TypeReference<List<LeasingContract>>() {}));
            }
        }
        catch (IOException e) {
            throw new FileException("Ошибка чтения файла: ", e);
        }
    }

    public void saveNewContract(List<LeasingContract> contracts, LeasingContract contract) {
        contracts.add(contract);
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, contracts);
        } catch (IOException e) {
            contracts.remove(contracts.size() - 1);
            throw new FileException("Ошибка чтения файла: ", e);
        }
    }
    public void updateFile(List<LeasingContract> contracts){
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, contracts);
        } catch (IOException e) {
            throw new FileException("Ошибка чтения файла: ", e);
        }
    }

    public void setClosedStatus(List<LeasingContract> contracts, LeasingContract contract){
        contract.setStatus(LeasingContract.Status.CLOSED);
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, contracts);
        } catch (IOException e) {
            contract.setStatus(LeasingContract.Status.ACTIVE);
            throw new FileException("Ошибка чтения файла: ", e);
        }
    }
}
