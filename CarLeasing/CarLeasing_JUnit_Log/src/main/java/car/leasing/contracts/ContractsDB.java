package car.leasing.contracts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ContractsDB {
    private final File file = new File("src/main/resources/contracts.json");
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isLoading = false;
    private final ExecutorService thread = Executors.newSingleThreadExecutor();
    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentsDB paymentsDB;

    private static final Logger log = LoggerFactory.getLogger(ContractsDB.class);

    public ContractsDB(){
        paymentsDB = new PaymentsDB();
        thread.execute(() -> {
            while (!Thread.interrupted()){
                try {
                    taskQueue.take().run();
                } catch (InterruptedException e) {
                    log.warn("Поток в договорах прерван");
                    isLoading = false;
                    Thread.currentThread().interrupt();
                    break;
                } catch (RuntimeException e){
                    isLoading = false;
                    log.error("Ошибка в потоке договоров: ", e);
                } finally {
                    if (taskQueue.isEmpty())
                        isLoading = false;
                }
            }
        });
    }
    public void readContracts(CopyOnWriteArrayList<LeasingContract> contracts) {
        isLoading = true;
        taskQueue.offer(() -> {
            isLoading = true;
            try {
                if (!file.exists()){
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    new ObjectMapper().writeValue(file, new ArrayList<>());
                }
                else{
                    try {
                        contracts.addAll(mapper.readValue(file, new TypeReference<List<LeasingContract>>() {}));
                    } catch (JsonMappingException e) {
                        System.out.println("Некорректный файл с договорами");
                        throw e;
                    }
                }
            } catch (IOException e) {
                System.out.println("Не удалось открыть/создать файл с договорами");
                log.error("Ошибка при чтении файла договоров: ", e);
                throw new RuntimeException(e);
            } finally {
                isLoading = false;
            }
        });

    }
    public CompletableFuture<Boolean> saveNewContract(CopyOnWriteArrayList<LeasingContract> contracts, LeasingContract contract){
        isLoading = true;
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        taskQueue.offer(() -> {
            isLoading = true;
            if (!paymentsDB.createContractPayments(contract)){
                isLoading = false;
                future.complete(false);
                return;
            }
            contracts.add(contract);
            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, contracts);
                future.complete(true);
            } catch(IOException e){
                log.error("Ошибка сохранения договора: ", e);
                contracts.removeLast();
                paymentsDB.deleteContractPayments(contract);
                future.complete(false);
            } finally {
                isLoading = false;
            }
        });
        return future;
    }
    public List<Payment> getPayments(LeasingContract contract) {
        return paymentsDB.getPaymentsByContractId(contract);
    }

    public Future<Boolean> payContract(CopyOnWriteArrayList<LeasingContract> contracts, LeasingContract contract){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        taskQueue.offer(() -> {
            if(!paymentsDB.pay(contract)){
                future.complete(false);
                return;
            }
            if (paymentsDB.getPaymentsByContractId(contract).stream()
                    .allMatch(p -> p.getStatus() == Payment.Status.PAID)) {
                contract.setStatus(LeasingContract.Status.CLOSED);
                try {
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    mapper.writeValue(file, contracts);
                    future.complete(true);
                } catch (Exception e) {
                    log.error("Ошибка записи в файл с договорами при оплате: " + e);
                    paymentsDB.repay(contract);
                    contract.setStatus(LeasingContract.Status.ACTIVE);
                    future.complete(false);
                }
            }
            else {
                future.complete(true);
            }
        });
        return future;
    }
    public boolean isAvailable(){
        return !isLoading;
    }
    public void finish(){
        taskQueue.offer(() ->  Thread.currentThread().interrupt());
        thread.shutdown();
        try {
            log.warn("Поток договоров заканчивает работу");
            thread.awaitTermination(10000000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Поток договоров не доработал");
            Thread.currentThread().interrupt();
        }
    }


    public Payment getCurrentPaymentByContract(LeasingContract contract) {
        return paymentsDB.getCurrentPaymentByContract(contract);
    }
}
